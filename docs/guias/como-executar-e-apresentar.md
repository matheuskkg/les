# Como executar e apresentar a aplicação

Este guia vale para qualquer módulo ou fluxo da Livraria: desenvolvimento, jar único, pacote para pendrive Windows e
testes E2E. A análise de vendas aparece como exemplo concreto no fim, mas os comandos e a estrutura servem para
vitrine, carrinho, checkout, pedidos, trocas, conta, assistente e o que mais for apresentado.

## 1. Visão geral

Há três formas de rodar o sistema:

| Modo | Quando usar | O que sobe |
|---|---|---|
| Desenvolvimento | programar e testar na sua máquina | server na 8080 e Vite na 5173 |
| Jar único | demonstrar sem Node e sem Vite | um processo Java na 8080, servindo API e web |
| Pacote portátil | apresentação presencial em Windows | jar + JRE + Node + Playwright em uma pasta |

O pacote portátil foi pensado para uma máquina Windows sem Java, Maven ou Node instalados. Ele ainda depende do
Google Chrome, que os testes usam como navegador.

## 2. Pré-requisitos

Para desenvolver e gerar o jar:

- Java 21 (o projeto compila com 21; o `mvn` pode rodar em um JDK mais novo, desde que o `release` continue 21);
- Maven 3.9 ou superior;
- Node.js 20 ou superior e npm;
- Google Chrome instalado, usado pelos testes E2E.

Para a apresentação presencial:

- uma máquina Windows 10 ou 11, 64 bits;
- Google Chrome instalado;
- a pasta do pacote copiada do pendrive. Não é preciso instalar Java, Maven ou Node na máquina.

Para montar o pacote no Linux, você também precisa do Python 3 e de baixar previamente um JRE 21 para Windows e um
Node para Windows. O jar é independente de sistema; o JRE e o Node não são.

## 3. Desenvolvimento na sua máquina

Abra dois terminais.

No primeiro, suba o back-end com o H2 em memória:

```bash
cd apps/server
mvn spring-boot:run -Dspring-boot.run.profiles=h2,dev
```

O server fica em `http://localhost:8080`. O schema é criado pelo Hibernate e o seed é executado na inicialização.
O profile `dev` ativa o provedor `fake`, então o assistente funciona sem rede e sem chave. Sem o `dev`, o `fake` fica
inativo e o assistente só responde com uma chave real configurada.

No segundo, suba o Vite:

```bash
cd apps/web
npm ci
npm run dev
```

Acesse `http://localhost:5173`. O `vite.config.ts` encaminha `/api` para a porta 8080, então o navegador vê uma origem
só, como no [DEC-09](../decisoes/DEC-09-topologia-de-execucao.md).

Credenciais do seed:

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin@livraria.test` | `Admin@123` |
| Cliente (Ana Souza) | `ana.souza@livraria.test` | `Senha@123` |

O banco do profile padrão é em memória. Parar e subir o server de novo zera as alterações e reaplica o seed. Para
usar PostgreSQL, siga o `apps/server/docker-compose.yml` e o profile `postgres`; para a apresentação, o H2 basta.

## 4. Jar único

O jar da demonstração serve a API e o build do web na mesma origem. A ordem importa: o Maven copia `apps/web/dist`
para dentro do jar, então o web precisa ser gerado antes.

```bash
cd apps/web
npm ci
npm run build

cd ../server
mvn -DskipTests package
```

O jar fica em `apps/server/target/livraria-server-0.0.1-SNAPSHOT.jar`. Para subir:

```bash
java -jar target/livraria-server-0.0.1-SNAPSHOT.jar
```

Acesse `http://localhost:8080`. Se o Maven for executado sem o `npm run build` antes, o jar sobe só com a API e a
página do web não aparece.

## 5. Testes

### Back-end

```bash
cd apps/server
mvn test
```

O `TempoDeRespostaTest` mede as consultas com o seed completo e exige no máximo 1 segundo, incluindo a análise e a
exportação. Para rodar só ele:

```bash
mvn test -Dtest=TempoDeRespostaTest
```

### Front-end

```bash
cd apps/web
npm test
npm run typecheck
```

### E2E de apresentação

O `playwright.config.ts` sobe o server com os profiles `h2,e2e,dev` e o Vite, e usa o Chrome instalado
(`channel: 'chrome'`). Cada teste chama `POST /api/teste/reset` antes de começar, volta o banco ao seed e zera o
relógio controlado. Essas rotas só existem no profile `e2e`.

Os E2E existem só para a apresentação. O Chrome abre visível e cada ação espera meio segundo, para o professor
acompanhar o que está sendo feito. No CI, o mesmo comando roda em modo headless e sem atraso.

| O que rodar | Comando |
|---|---|
| Todos os roteiros | `npm run e2e` |
| Um fluxo | `npm run e2e -- e2e/apresentacaoAnalise.spec.ts` |
| Um cenário do fluxo | `npm run e2e -- e2e/apresentacaoAnalise.spec.ts -g "6\."` |

O relatório fica em `apps/web/playwright-report` e pode ser aberto com:

```bash
npx playwright show-report
```

### Roteiro de apresentação de um fluxo

Todo E2E do projeto é um roteiro de apresentação. O padrão de nomes é:

| Tipo | Padrão | Exemplo |
|---|---|---|
| Roteiro de apresentação | `apresentacao<Fluxo>.spec.ts` | `apresentacaoAnalise.spec.ts` |
| Apoio compartilhado | `fixtures.ts` | `fixtures.ts` |

Os testes de apresentação são specs que começam com `apresentacao`, por exemplo
`apps/web/e2e/apresentacaoAnalise.spec.ts`. Eles seguem o `fixtures.ts` compartilhado, e o nome permite rodar todos
ou escolher um fluxo:

```bash
npm run e2e
npm run e2e -- e2e/apresentacaoAnalise.spec.ts
```

Para apresentar outro módulo, crie `apps/web/e2e/apresentacao<Fluxo>.spec.ts` seguindo o exemplo da análise. Escolha
os passos que o professor vai ver, na ordem em que ele vê, e deixe cada `test` contar um cenário. Use o login e as
telas reais, não chamadas diretas à API.

Não crie um script por fluxo no `package.json`: use `npm run e2e -- e2e/apresentacao<Fluxo>.spec.ts`. O pacote portátil copia a
pasta `e2e` inteira; para continuar autossuficiente, o roteiro deve importar só `@playwright/test`, `./fixtures` e
helpers que estejam dentro de `e2e/`; evite importar código da aplicação.

### E2E contra o jar empacotado

O pacote de apresentação usa o jar, não o Vite. Para reproduzir isso na sua máquina:

```bash
# terminal 1
cd apps/server
java -jar target/livraria-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2,e2e

# terminal 2
cd apps/web
npm run e2e -- -c playwright.apresentacao.config.ts
```

O `playwright.apresentacao.config.ts` não sobe processo nenhum: ele espera a aplicação em `http://localhost:8080`.
Por isso o server precisa estar rodando com o profile `e2e`, que expõe as rotas de reset e de relógio usadas pelos
testes. O Chrome abre visível e as ações continuam desaceleradas.

## 6. Pacote para o pendrive Windows

### 6.1 O que baixar

Baixe e extraia, no seu Linux:

1. **JRE 21 para Windows x64**, de preferência o Temurin JRE da Adoptium
   (`https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64&package=jre`). O diretório extraído deve
   ter `bin/java.exe`.
2. **Node.js 20 ou superior para Windows x64**, em formato `.zip`
   (`https://nodejs.org/en/download`). O arquivo `node.exe` fica na raiz do diretório extraído.

O Chrome deve estar instalado na máquina da apresentação. O pacote não baixa navegador.

### 6.2 Montar o pacote

O script `scripts/preparar-apresentacao.py` compila o web, empacota o server, copia os testes e monta os launchers.
Ele não baixa nada; os caminhos do JRE e do Node são seus.

```bash
python3 scripts/preparar-apresentacao.py \
  --jre ~/portateis/jre-21-windows \
  --node ~/portateis/node-v22-windows/node.exe
```

O resultado fica em `dist/apresentacao`:

```text
dist/apresentacao/
  livraria.jar
  runtime/                 JRE 21 para Windows
  node/node.exe            Node para os testes
  iniciar-app.bat
  rodar-testes.bat
  LEIA-ME.txt
  GUIA.md                  cópia deste guia
  testes/
    playwright.apresentacao.config.ts
    e2e/                   specs e apoio; o testMatch roda só apresentacao*.spec.ts
    node_modules/          @playwright/test, playwright e playwright-core
```

Se você já gerou o jar e o `dist` do web, e só quer remontar a pasta:

```bash
python3 scripts/preparar-apresentacao.py --pular-build \
  --jre ~/portateis/jre-21-windows \
  --node ~/portateis/node-v22-windows/node.exe
```

Se o jar estiver em outro caminho, acrescente `--jar /caminho/do/livraria.jar`.

Se preferir montar em uma máquina Windows, instale Python 3 e rode o mesmo script com caminhos do Windows. As opções
`--jre` e `--node` aceitam o diretório do JRE e o `node.exe`. Para uma apresentação em Linux, use `--so linux` e
informe um JRE e um Node Linux; o script troca os launchers `.bat` por `.sh`.

### 6.3 Na máquina da apresentação

1. Copie a pasta `dist/apresentacao` do pendrive para o disco local, por exemplo `C:\apresentacao`. Rodar do
   pendrive funciona, mas o disco local abre mais rápido.
2. Abra o Chrome.
3. Dê um duplo clique em `iniciar-app.bat`. A janela do console fica aberta enquanto o sistema estiver rodando.
4. Acesse `http://localhost:8080` e entre com o perfil do fluxo que será apresentado.
5. Para os testes, deixe a janela do sistema aberta e dê um duplo clique em `rodar-testes.bat`. Ele verifica se a
   aplicação está respondendo, abre o Chrome e roda todos os roteiros `apresentacao*.spec.ts`.

O sistema usa H2 em memória. Reiniciar o `iniciar-app.bat` zera as alterações e reaplica o seed. O profile `e2e`
usado pelos launchers desliga as tarefas agendadas de expiração e expõe as rotas de teste; ele não ativa o `fake`.
Para a IA real na apresentação, coloque o `.env` com as chaves na mesma pasta do `livraria.jar` (o
`application.yml` lê `optional:file:.env`) ou defina as variáveis `IA_*_CHAVE` no sistema antes de abrir o
`iniciar-app`.

Se o Chrome não estiver instalado, instale-o ou troque `channel: 'chrome'` por `channel: 'msedge'` em
`testes/playwright.apresentacao.config.ts`. No desenvolvimento, o mesmo ajuste vale para `playwright.config.ts`. O
Edge já vem no Windows.

### 6.4 Se a porta 8080 estiver ocupada

Feche o outro processo ou troque a porta nos dois lugares:

- em `iniciar-app.bat`, no argumento `--server.port=8080`;
- em `testes/playwright.apresentacao.config.ts`, na `baseURL`.

Se usar outra porta, o `rodar-testes.bat` também precisa verificar a nova porta na chamada ao `/v3/api-docs`.

### 6.5 Sem internet

A apresentação usa os provedores reais de IA; o `fake` fica inativo. Sem internet ou sem chave configurada, o
assistente fica indisponível. O restante do sistema continua funcionando, porque o banco é H2 em memória. Para
ensaiar sem rede com os testes, acrescente o profile `dev` (`h2,e2e,dev`); ele ativa o `fake`.

## 7. Exemplo: análise de vendas

Esta seção é só um exemplo de roteiro. O mesmo formato vale para qualquer outro fluxo.

A tela **Análise de vendas** implementa RF-ANA-001 a RF-ANA-004 e RNF-ANA-001 a RNF-ANA-004. O administrador escolhe
um período em meses, marca uma ou mais categorias e vê um gráfico de linhas com uma série por categoria, além da
tabela com os mesmos valores e da exportação em CSV.

### 7.1 O que o DRS manda contar

O texto original do DRS, registrado no DVP e no [DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md), manda somar
pedidos **APROVADA**, **EM TRANSPORTE** e **ENTREGUE**. Pedidos **REPROVADA** e cancelados não entram. O sistema segue
essa regra. A atividade da disciplina pede no critério (d) somente **ENTREGUE**, o que é uma divergência em relação ao
DRS.

Como a decisão do projeto foi manter o DRS, a demonstração deve explicar essa escolha. O cenário 5 do roteiro abaixo
usa um pedido REPROVADA, que não entra nas duas leituras. O cenário 6 mostra o comportamento real do DRS: um pedido
que já estava EM TRANSPORTE já contava e continua contando depois de ENTREGUE. Se o professor exigir o critério da
atividade, mostre o texto do RN-ANA-004 e explique que o projeto priorizou o requisito original.

Os valores seguem o mês da finalização do pedido, sem frete e sem rateio do desconto promocional. O valor de cada
item é o preço unitário multiplicado pela quantidade não trocada. Um livro com duas categorias entra com o valor
cheio em cada uma.

### 7.2 Roteiro de demonstração

Os oito cenários estão no arquivo `apps/web/e2e/apresentacaoAnalise.spec.ts`. Rodar o arquivo inteiro leva cerca de
15 segundos e mostra cada teste no relatório.

| Cenário da atividade | Teste | O que aparece |
|---|---|---|
| 1. Uma categoria em um período válido | teste 1 | Romance em 03/2025 com R$ 95,76, igual à soma do seed |
| 2. Comparação entre categorias | teste 2 | História e Romance no mesmo mês, cada uma com R$ 95,76 |
| 3. Gráfico de linhas em reais por mês | teste 3 | Fantasia e Romance de 03/2025 a 04/2025, no gráfico e na tabela |
| 4. Alteração da data de início ou fim | teste 4 | Fim muda de 03/2025 para 04/2025 e o valor de Romance passa a R$ 168,99 |
| 5. Compra com status diferente de ENTREGUE | teste 5 | Tecnologia em 03/2025 vale R$ 0,00 porque o único pedido é REPROVADA |
| 6. Compra que passa a ENTREGUE | teste 6 | Pedido 242, EM TRANSPORTE, é entregue em 10/2026 e continua no gráfico |
| 7. Acesso só do administrador | teste 7 | Cliente entra e recebe "Acesso negado" ao abrir `/admin/analise` |
| 8. Resposta em até 1 segundo | teste 8 | A consulta de 03/2025 responde em menos de 1 s; o backend também tem o `TempoDeRespostaTest` |

Para rodar só a apresentação da análise:

```bash
cd apps/web
npm run e2e -- e2e/apresentacaoAnalise.spec.ts
```

Para rodar um cenário isolado:

```bash
npm run e2e -- e2e/apresentacaoAnalise.spec.ts -g "6\\."
```

No pacote portátil, o mesmo filtro funciona com:

```bat
rodar-testes.bat -g "6\."
```

## 8. Credenciais e dados de teste

Credenciais:

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin@livraria.test` | `Admin@123` |
| Cliente (Ana Souza) | `ana.souza@livraria.test` | `Senha@123` |

O seed histórico da análise vai de novembro de 2024 a outubro de 2026. Março de 2025 é o mês de referência dos
testes:

| Categoria | Valor em 03/2025 | Origem |
|---|---:|---|
| Romance | R$ 95,76 | 2 × Dom Casmurro |
| História | R$ 95,76 | 2 × Dom Casmurro, que também é de História |
| Ficção Científica | R$ 96,00 | 3 × Fundação menos 1 unidade em troca concluída |
| Fantasia | R$ 80,00 | 2 × O Hobbit, com troca ainda autorizada |
| Tecnologia | R$ 0,00 | o único pedido do mês é REPROVADA |

Em outubro de 2026 há pedidos ENTREGUE, EM TRANSPORTE, APROVADA e REPROVADA. O teste 6 usa o pedido 242, que está
EM TRANSPORTE e tem itens de Fantasia, Ficção Científica e História.

## 9. Problemas comuns

**A página abre só com a API ou dá 404 no navegador.** O `mvn package` rodou antes do `npm run build`. Gere o web e
empacote de novo.

**`mvn` falha por versão de Java.** Confira `java -version` e `mvn -version`. O projeto usa Java 21; se o Maven estiver
apontando para outro JDK, ajuste `JAVA_HOME`.

**O Playwright não acha o Chrome.** Instale o Chrome ou use `channel: 'msedge'` no arquivo de configuração. O projeto
não baixa navegadores ([DEC-02](../decisoes/DEC-02-framework-e2e.md)).

**O E2E reclama do `/api/teste/reset`.** O server não está com o profile `e2e`. Suba com
`--spring.profiles.active=h2,e2e`.

**O jar não encontra o `runtime`.** O JRE precisa ser da mesma arquitetura e do mesmo sistema da máquina de destino.
Um runtime Linux não roda no Windows. Baixe o JRE Windows x64 ou monte o pacote na própria máquina Windows.

**O Windows bloqueia o `.bat`.** Libere a pasta no SmartScreen ou execute pelo Prompt de Comando com
`iniciar-app.bat`.

**Os testes apagaram os dados que eu tinha acabado de criar.** É o reset do E2E. Ele volta ao seed antes de cada
teste. Reinicie o `iniciar-app.bat` se quiser começar de novo.

**O comando `e2e` não encontra nenhum teste.** Crie um arquivo `e2e/apresentacao<Fluxo>.spec.ts` ou use
`npm run e2e -- e2e/apresentacaoAnalise.spec.ts`.

## 10. Onde estão as decisões

- [DEC-09](../decisoes/DEC-09-topologia-de-execucao.md): topologia dos processos e rotas de apoio do E2E.
- [DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md): exemplo de regra de negócio da análise.
- [DEC-15.4](../decisoes/DEC-15.4-acesso-consultas-e-exportacao.md): exportação em CSV e autorização.
- [DEC-02](../decisoes/DEC-02-framework-e2e.md): Playwright com o Chrome instalado.
- [Requisitos da análise](../requisitos/analise.md): descrições originais e matriz de rastreabilidade.
