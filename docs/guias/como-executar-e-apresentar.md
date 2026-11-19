# Como executar e apresentar a aplicação

Este guia vale para qualquer módulo ou fluxo da Livraria: desenvolvimento, jar único, pacote para pendrive Windows e
testes E2E. Os comandos são os mesmos para todos; o que muda é o roteiro de apresentação escolhido na interface do
Playwright. Há roteiros para cadastro de clientes, carrinho, checkout, pedidos, trocas e análise de vendas, e o mesmo
padrão serve para qualquer outro fluxo.

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

O `TempoDeRespostaTest` mede as consultas com o seed completo — vitrine, clientes, pedidos, trocas e análise — e exige
no máximo 1 segundo, incluindo a exportação em CSV. Para rodar só ele:

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

Os E2E existem só para a apresentação. O comando abre o Playwright UI Mode: a lista de roteiros fica de um lado e,
ao executar um cenário, o Chrome abre maximizado na resolução da tela, com cada ação esperando meio segundo. Quando a
validação é sobre um valor que pode ficar abaixo da dobra, o teste rola até o alvo antes de conferir, para o professor
ver o que está sendo validado. A interface permite rodar todos, repetir um cenário e voltar passo a passo. No CI,
`npx playwright test` roda em modo headless e sem atraso.

Quando o login não é o cenário, o teste começa já autenticado pela API e vai direto à tela do fluxo. O login e o
cadastro pela tela ficam reservados ao roteiro que quer demonstrá-los; dados de apoio, como um usuário para outro
cenário, também podem ser criados pela API.

| O que abrir | Comando |
|---|---|
| Todos os roteiros | `npm run e2e` |
| Um fluxo | `npm run e2e -- e2e/apresentacao<Fluxo>.spec.ts` |
| Cadastro de clientes | `npm run e2e -- e2e/apresentacaoClientes.spec.ts` |
| Carrinho e estoque | `npm run e2e -- e2e/apresentacaoCarrinho.spec.ts` |
| Compra e pagamento | `npm run e2e -- e2e/apresentacaoCheckout.spec.ts` |
| Pedidos e trocas | `npm run e2e -- e2e/apresentacaoPedidosETrocas.spec.ts` |
| Análise de vendas | `npm run e2e -- e2e/apresentacaoAnalise.spec.ts` |
| Um cenário do fluxo | `npm run e2e -- e2e/apresentacao<Fluxo>.spec.ts -g "3\."` (o número é o do título do `test`) |

No pacote portátil, o mesmo filtro funciona com `rodar-testes.bat -g "3\."`.

Na janela do Playwright UI, clique em **Run all** ou no botão de executar ao lado do cenário. O botão de repetir roda
de novo, e a linha do tempo de cada passo permite voltar ao estado anterior.

O modo UI não gera o relatório HTML: o acompanhamento é na própria interface. Para gerar o relatório depois de uma
execução sem interface (`npx playwright test`), ele fica em `apps/web/playwright-report` e pode ser aberto com:

```bash
npx playwright show-report
```

### Roteiro de apresentação de um fluxo

Todo E2E do projeto é um roteiro de apresentação. O padrão de nomes é:

| Tipo | Padrão | Exemplos |
|---|---|---|
| Roteiro de apresentação | `apresentacao<Fluxo>.spec.ts` | `apresentacaoClientes.spec.ts`, `apresentacaoCarrinho.spec.ts`, `apresentacaoCheckout.spec.ts`, `apresentacaoPedidosETrocas.spec.ts`, `apresentacaoAnalise.spec.ts` |
| Apoio compartilhado | `fixtures.ts` | `fixtures.ts` |

Os testes de apresentação são specs que começam com `apresentacao`, ficam em `apps/web/e2e/` e seguem o `fixtures.ts`
compartilhado. O prefixo permite rodar todos ou escolher um fluxo:

```bash
npm run e2e
npm run e2e -- e2e/apresentacao<Fluxo>.spec.ts
```

Para apresentar outro módulo, crie `apps/web/e2e/apresentacao<Fluxo>.spec.ts` seguindo um roteiro existente. Escolha
os passos que o professor vai ver, na ordem em que ele vê, e deixe cada `test` contar um cenário, com o requisito no
título. Use as telas reais para o que o cenário demonstra. Quando o login ou o cadastro não é o assunto, o teste começa
já autenticado pela API (`entrarNaApi`) e vai direto à tela do fluxo; dados de apoio também podem ser criados pela API,
sem validar em tela. Escolha filtros e dados que deem ao professor uma cena representativa (uma tabela com várias
linhas, um gráfico com evolução, status variados), não o mínimo que só faz a asserção passar. Antes de validar conteúdo
que pode ficar abaixo da dobra, role até o alvo com `rolarAte`, para o professor ver o que está sendo conferido.

Os cenários de cada fluxo estão documentados nos títulos dos `test` do respectivo spec e, quando houver, na matriz de
rastreabilidade do requisito em [`docs/requisitos/`](../requisitos/README.md). A ordem dos cenários é definida pelo que
faz sentido na apresentação de cada fluxo.

Não crie um script por fluxo no `package.json`: use `npm run e2e -- e2e/apresentacao<Fluxo>.spec.ts`. O pacote
portátil copia a pasta `e2e` inteira; para continuar autossuficiente, o roteiro deve importar só `@playwright/test`,
`./fixtures` e helpers que estejam dentro de `e2e/`; evite importar código da aplicação.

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
testes. O Playwright UI abre junto com o Chrome maximizado, e as ações continuam desaceleradas.

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
   aplicação está respondendo e abre o Playwright UI com os roteiros `apresentacao*.spec.ts`. Na interface, rode todos
   ou escolha um cenário; o Chrome abre maximizado para a apresentação.

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

## 7. Credenciais e dados de teste

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin@livraria.test` | `Admin@123` |
| Cliente (Ana Souza) | `ana.souza@livraria.test` | `Senha@123` |

O seed cria os livros, o estoque, as tabelas de domínio e um histórico de pedidos e trocas. Cada roteiro escolhe os
dados que precisa; quando o cenário não depende deles, crie os dados de apoio pela API com `apiDaPagina` e `tokenCsrf`,
sem validá-los em tela. Como o reset roda antes de cada teste, um roteiro nunca depende do que o anterior deixou no
banco. Para entender os dados e as regras de um fluxo específico, veja a matriz de rastreabilidade correspondente em
[`docs/requisitos/`](../requisitos/README.md).

## 8. Problemas comuns

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

**O comando `e2e` não encontra nenhum teste.** Crie um arquivo `e2e/apresentacao<Fluxo>.spec.ts` ou rode um fluxo
existente, como `npm run e2e -- e2e/apresentacaoAnalise.spec.ts`.

## 9. Onde estão as decisões

- [DEC-09](../decisoes/DEC-09-topologia-de-execucao.md): topologia dos processos e rotas de apoio do E2E.
- [DEC-02](../decisoes/DEC-02-framework-e2e.md): Playwright com o Chrome instalado.
- [DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md): status de pedido e trocas, regra compartilhada por vários
  fluxos.
- [DEC-15.4](../decisoes/DEC-15.4-acesso-consultas-e-exportacao.md): acesso às consultas e exportação em CSV.
- [Índice de decisões](../decisoes/README.md) e [índice de requisitos](../requisitos/README.md): decisões e requisitos
  de todos os módulos.
