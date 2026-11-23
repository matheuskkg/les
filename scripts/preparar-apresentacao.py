#!/usr/bin/env python3
"""Monta a pasta portátil de apresentação da Livraria.

O pacote leva o jar (com o web já embutido), um runtime Java, o Node para o Playwright e os testes de apresentação.
O runtime e o Node são específicos do sistema operacional de destino; por isso o script recebe os caminhos das
distribuições já baixadas. O resto do conteúdo é copiado do próprio repositório.

Exemplos:

    # Pacote Windows montado no Linux (baixe antes o JRE 21 e o Node para Windows)
    python3 scripts/preparar-apresentacao.py \\
        --jre ~/portateis/jre-21-windows \\
        --node ~/portateis/node-v22-windows/node.exe

    # Pacote Linux, usando o runtime gerado pelo jlink e o Node da máquina
    python3 scripts/preparar-apresentacao.py --so linux \\
        --jre dist/runtime-linux --node "$(command -v node)"

    # Só remonta o pacote, sem rodar npm/mvn (o jar e o dist já precisam existir)
    python3 scripts/preparar-apresentacao.py --pular-build --jre ... --node ...
"""

from __future__ import annotations

import argparse
import os
import platform
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
WEB = RAIZ / "apps" / "web"
SERVER = RAIZ / "apps" / "server"
PACOTES_DO_PLAYWRIGHT = ("@playwright/test", "playwright", "playwright-core")
GUIA = RAIZ / "docs" / "guias" / "como-executar-e-apresentar.md"


def executar(comando: list[str], diretorio: Path) -> None:
    # No Windows, npm e mvn são .cmd; o CreateProcess não os executa direto.
    if os.name == "nt" and Path(comando[0]).suffix.lower() in (".cmd", ".bat"):
        comando = ["cmd.exe", "/c", *comando]
    # flush: sem ele, a linha do comando aparece depois da saída do próprio comando quando a saída é canalizada.
    print(f"$ {' '.join(comando)}  ({diretorio.relative_to(RAIZ)})", flush=True)
    resultado = subprocess.run(comando, cwd=diretorio, text=True)
    if resultado.returncode != 0:
        raise SystemExit(f"Falhou: {' '.join(comando)}")


def ferramenta(nome: str) -> str:
    caminho = shutil.which(nome)
    if caminho is None:
        raise SystemExit(f"Não encontrei {nome} no PATH. Instale ou use --pular-build com o jar já gerado.")
    return caminho


def validar_jar(jar: Path) -> Path:
    if not jar.is_file():
        raise SystemExit(f"Jar não encontrado: {jar}")
    with zipfile.ZipFile(jar) as arquivo:
        if "BOOT-INF/classes/static/index.html" not in arquivo.namelist():
            raise SystemExit(
                "O jar não tem o build do web. Rode `npm run build` em apps/web antes de empacotar o server."
            )
    return jar


def construir(pular: bool) -> Path:
    if not pular:
        if not (WEB / "node_modules").is_dir():
            executar([ferramenta("npm"), "ci"], WEB)
        executar([ferramenta("npm"), "run", "build"], WEB)
        # clean evita levar em static/ assets de builds anteriores: os nomes com hash mudam a
        # cada build e o maven-resources não apaga os antigos de target/classes.
        executar([ferramenta("mvn"), "-DskipTests", "clean", "package"], SERVER)

    jars = [jar for jar in (SERVER / "target").glob("*.jar") if not jar.name.endswith(".original")]
    if not jars:
        raise SystemExit("Jar não encontrado em apps/server/target. Rode o build antes.")
    return validar_jar(max(jars, key=lambda caminho: caminho.stat().st_size))


def copiar_jre(origem: Path, destino: Path, sistema: str) -> None:
    if not origem.is_dir():
        raise SystemExit(f"JRE não encontrada: {origem}")
    executavel = origem / "bin" / ("java.exe" if sistema == "windows" else "java")
    if not executavel.exists():
        raise SystemExit(f"{origem} não parece um JRE de {sistema}: falta {executavel.relative_to(origem)}.")
    shutil.copytree(origem, destino)
    if sistema != "windows":
        (destino / "bin" / "java").chmod(0o755)


def copiar_node(origem: Path, destino: Path, sistema: str) -> None:
    if origem.is_dir():
        origem = origem / ("node.exe" if sistema == "windows" else "node")
    if not origem.is_file():
        raise SystemExit(f"Node não encontrado: {origem}")
    destino.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(origem, destino)
    if sistema != "windows":
        destino.chmod(0o755)


def copiar_testes(destino: Path) -> None:
    testes = destino / "testes"
    roteiros = sorted((WEB / "e2e").glob("apresentacao*.spec.ts"))
    if not roteiros:
        raise SystemExit("Nenhum roteiro de apresentação encontrado em apps/web/e2e/apresentacao*.spec.ts.")
    # A pasta inteira entra para que um roteiro novo possa reutilizar helpers dos E2E comuns.
    shutil.copytree(WEB / "e2e", testes / "e2e")
    shutil.copy2(WEB / "playwright.apresentacao.config.ts", testes / "playwright.apresentacao.config.ts")

    origens = WEB / "node_modules"
    for pacote in PACOTES_DO_PLAYWRIGHT:
        shutil.copytree(origens / pacote, testes / "node_modules" / pacote)
    (testes / "package.json").write_text(
        '{\n  "name": "livraria-testes",\n  "private": true,\n  "type": "module"\n}\n',
        encoding="utf-8",
    )


def escrever(caminho: Path, conteudo: str, *, windows: bool = False) -> None:
    if windows:
        conteudo = conteudo.replace("\n", "\r\n")
    caminho.write_text(conteudo, encoding="utf-8", newline="")


def criar_launchers_windows(destino: Path) -> None:
    escrever(
        destino / "iniciar-app.bat",
        """@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
set "JAVA=%~dp0runtime\\bin\\java.exe"
if not exist "%JAVA%" (
  echo ERRO: runtime\\bin\\java.exe nao encontrado.
  echo Copie um JRE 21 para Windows para a pasta runtime.
  pause
  exit /b 1
)
if not exist "%~dp0livraria.jar" (
  echo ERRO: livraria.jar nao encontrado.
  pause
  exit /b 1
)
echo.
echo Livraria - apresentacao
echo Abra http://localhost:8080 no Chrome.
echo Feche esta janela para encerrar o sistema.
echo.
"%JAVA%" -Dfile.encoding=UTF-8 -jar "%~dp0livraria.jar" --spring.profiles.active=h2,e2e --server.port=8080
pause
""",
        windows=True,
    )
    escrever(
        destino / "rodar-testes.bat",
        """@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
set "NODE=%~dp0node\\node.exe"
set "CLI=%~dp0testes\\node_modules\\@playwright\\test\\cli.js"
if not exist "%NODE%" (
  echo ERRO: node\\node.exe nao encontrado.
  pause
  exit /b 1
)
if not exist "%CLI%" (
  echo ERRO: testes\\node_modules\\@playwright\\test\\cli.js nao encontrado.
  pause
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/v3/api-docs' -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } } catch {}; exit 1"
if errorlevel 1 (
  echo ERRO: a aplicacao nao esta respondendo em http://localhost:8080.
  echo Execute iniciar-app.bat em outra janela e tente de novo.
  pause
  exit /b 1
)
echo.
echo Abrindo o Playwright UI com os testes de apresentacao...
echo.
"%NODE%" "%CLI%" test -c "%~dp0testes\\playwright.apresentacao.config.ts" --ui %*
echo.
echo Interface encerrada.
pause
""",
        windows=True,
    )


def criar_launchers_linux(destino: Path) -> None:
    escrever(
        destino / "iniciar-app.sh",
        """#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
exec ./runtime/bin/java -Dfile.encoding=UTF-8 -jar livraria.jar \\
  --spring.profiles.active=h2,e2e --server.port=8080
""",
    )
    escrever(
        destino / "rodar-testes.sh",
        """#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if ! curl -fsS http://localhost:8080/v3/api-docs >/dev/null; then
  echo "ERRO: a aplicação não está respondendo em http://localhost:8080." >&2
  echo "Execute ./iniciar-app.sh em outra janela." >&2
  exit 1
fi
exec ./node/node ./testes/node_modules/@playwright/test/cli.js test \\
  -c ./testes/playwright.apresentacao.config.ts --ui "$@"
""",
    )
    (destino / "iniciar-app.sh").chmod(0o755)
    (destino / "rodar-testes.sh").chmod(0o755)


def criar_leia_me(destino: Path, sistema: str) -> None:
    launcher = "iniciar-app.bat" if sistema == "windows" else "./iniciar-app.sh"
    testes = "rodar-testes.bat" if sistema == "windows" else "./rodar-testes.sh"
    escrever(
        destino / "LEIA-ME.txt",
        f"""LIVRARIA - PACOTE DE APRESENTACAO

Conteudo:
  livraria.jar      sistema completo (API + web)
  runtime/          Java 21 portatil
  node/             Node portatil, usado so pelos testes
  testes/           Playwright + roteiros de apresentacao
  {launcher:<17} sobe o sistema em http://localhost:8080
  {testes:<17} abre o Playwright UI com os roteiros

Como apresentar:
  1. Abra o Chrome na maquina.
  2. Execute {launcher}.
  3. Acesse http://localhost:8080.
  4. Entre como administrador:
       e-mail: admin@livraria.test
       senha:  Admin@123
  5. Com o sistema aberto, execute {testes} em outra janela.
     A interface do Playwright abre; rode todos ou escolha um cenario, e o Chrome
     percorre os roteiros de apresentacao.

Observacoes:
  - O sistema usa H2 em memoria: reiniciar zera as alteracoes e reaplica o seed.
  - O profile "e2e" expoe as rotas de reset usadas pelos testes. Fora da apresentacao, rode sem ele.
  - O Chrome precisa estar instalado. Se so houver Edge, troque "channel: 'chrome'" por
    "channel: 'msedge'" em testes/playwright.apresentacao.config.ts.
  - Se a porta 8080 estiver ocupada, altere a porta em {launcher} e a baseURL em
    testes/playwright.apresentacao.config.ts.
""",
        windows=sistema == "windows",
    )
    if GUIA.exists():
        shutil.copy2(GUIA, destino / "GUIA.md")


def main() -> None:
    parser = argparse.ArgumentParser(description="Monta a pasta portatil de apresentacao.")
    parser.add_argument("--destino", type=Path, help="pasta de saida (padrao: dist/apresentacao)")
    parser.add_argument("--jre", type=Path, required=True, help="raiz do JRE do sistema de destino")
    parser.add_argument("--node", type=Path, required=True, help="node.exe (Windows) ou node (Linux)")
    parser.add_argument("--jar", type=Path, help="jar pronto; sem ele, o script procura em apps/server/target")
    parser.add_argument("--pular-build", action="store_true", help="nao roda npm/mvn; usa o jar existente")
    parser.add_argument("--so", choices=("windows", "linux", "auto"), default="windows", help="sistema de destino")
    args = parser.parse_args()

    sistema = platform.system().lower() if args.so == "auto" else args.so
    if sistema.startswith("win"):
        sistema = "windows"
    elif sistema.startswith("linux"):
        sistema = "linux"
    else:
        raise SystemExit(f"Sistema de destino não suportado: {sistema}")

    destino = args.destino or (RAIZ / "dist" / ("apresentacao" if sistema == "windows" else "apresentacao-linux"))
    destino = destino.resolve()

    jar = validar_jar(args.jar.resolve()) if args.jar else construir(args.pular_build)

    if destino.exists():
        shutil.rmtree(destino)
    destino.mkdir(parents=True)

    shutil.copy2(jar, destino / "livraria.jar")
    copiar_jre(args.jre.resolve(), destino / "runtime", sistema)
    copiar_node(args.node.resolve(), destino / "node" / ("node.exe" if sistema == "windows" else "node"), sistema)
    copiar_testes(destino)
    if sistema == "windows":
        criar_launchers_windows(destino)
    else:
        criar_launchers_linux(destino)
    criar_leia_me(destino, sistema)

    print()
    print(f"Pacote pronto em: {destino}")
    print("Copie a pasta inteira para o pendrive e execute o launcher na maquina da apresentacao.")


if __name__ == "__main__":
    main()
