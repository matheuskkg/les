#!/usr/bin/env bash
# Sobe a aplicação completa em modo de desenvolvimento:
#   - server Spring Boot (H2 em memória) em http://localhost:8080
#   - web (Vite) em http://localhost:5173, com /api e /v3 encaminhados para o server
#
# Acesse http://localhost:5173. O assistente de IA usa os provedores reais, então
# configure as variáveis IA_*_CHAVE (ou um arquivo .env em apps/server) antes de rodar.
# Para usar o assistente sem chave nem rede, troque a linha do mvn abaixo por
#   mvn spring-boot:run -Dspring-boot.run.profiles=h2,dev
#
# Uso: scripts/iniciar-apps.sh
# Ctrl+C encerra os dois processos.

set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Livraria — ambiente de desenvolvimento"

# Pré-requisitos (docs/guias/como-executar-e-apresentar.md, seção 2).
for comando in java mvn node npm; do
  if ! command -v "$comando" >/dev/null 2>&1; then
    echo "Erro: '$comando' não encontrado no PATH. Instale o pré-requisito e tente de novo." >&2
    exit 1
  fi
done

PID_SERVER=""
PID_WEB=""

encerrar() {
  trap - INT TERM
  echo
  echo "==> Encerrando server e web..."
  for pid in "$PID_WEB" "$PID_SERVER"; do
    [[ -n "$pid" ]] || continue
    # Encerra os filhos antes do processo: o npm lança o Vite como filho.
    if command -v pgrep >/dev/null 2>&1; then
      for filho in $(pgrep -P "$pid" 2>/dev/null || true); do
        kill "$filho" 2>/dev/null || true
      done
    fi
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}
trap encerrar INT TERM

echo "==> Subindo o server (H2, IA real) em http://localhost:8080 ..."
(
  cd "$RAIZ/apps/server"
  exec mvn spring-boot:run
) &
PID_SERVER=$!

echo "==> Subindo o web em http://localhost:5173 ..."
(
  cd "$RAIZ/apps/web"
  if [[ ! -d node_modules ]]; then
    echo "    Instalando dependências do web (npm ci)..."
    npm ci
  fi
  exec npm run dev
) &
PID_WEB=$!

echo
echo "Aplicação no ar:"
echo "  Web:     http://localhost:5173"
echo "  API:     http://localhost:8080/api"
echo "  Swagger: http://localhost:8080/v3/api-docs"
echo "  Logins do seed: admin@livraria.test / Admin@123  |  ana.souza@livraria.test / Senha@123"
echo "Pressione Ctrl+C para encerrar."

# Se qualquer um dos dois processos cair, derruba o outro também.
set +e
wait -n "$PID_SERVER" "$PID_WEB"
status=$?
set -e
encerrar
exit "$status"
