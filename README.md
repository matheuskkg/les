# Livraria

E-commerce de livros desenvolvido para a disciplina de Laboratório de Engenharia de Software (Fatec, 2026).

## Estrutura

- `apps/server`: back-end em Java 21 e Spring Boot (Maven)
- `apps/web`: front-end em Preact, Vite e TypeScript
- `docs`: requisitos, decisões do projeto, arquitetura e guias

## Requisitos

- Java 21 e Maven
- Node.js e npm
- Docker (opcional, para o PostgreSQL)

## Como executar

O passo a passo completo, incluindo o pacote de apresentação para Windows com JRE e Node portáteis, está em
[`docs/guias/como-executar-e-apresentar.md`](docs/guias/como-executar-e-apresentar.md).

Back-end (H2 em memória, porta 8080):

```bash
cd apps/server
mvn spring-boot:run
```

Front-end (porta 5173, encaminha `/api` para o back-end):

```bash
cd apps/web
npm install
npm run dev
```

Acesse http://localhost:5173.

Para usar o PostgreSQL, suba o banco com `docker compose up -d` em `apps/server` e rode com
`mvn spring-boot:run -Dspring-boot.run.profiles=postgres`.

## Testes

```bash
cd apps/server && mvn test      # back-end
cd apps/web && npm test         # front-end
cd apps/web && npm run e2e      # E2E no Playwright UI Mode (usa o Chrome instalado)
```

Os E2E são roteiros de apresentação e abrem o Playwright UI Mode, onde você escolhe o cenário, acompanha cada passo,
repete a execução e volta no tempo. O Chrome abre maximizado e, quando o alvo da validação fica abaixo da dobra, o
teste rola até ele. Use `npm run e2e` para abrir todos no UI Mode ou
`npm run e2e -- e2e/apresentacaoAnalise.spec.ts` para um fluxo; os detalhes estão no
[guia](docs/guias/como-executar-e-apresentar.md#5-testes). Para rodar sem a interface, como no CI, use
`npx playwright test`.
