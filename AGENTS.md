# AGENTS.md

Guia para agentes de IA (Claude, Codex etc.) trabalhando neste repositório.

## Projeto

E-commerce de livros **single-tenant** (uma única loja), trabalho da disciplina LES (Fatec, 2026).
Fonte dos requisitos: DRS v0.6, transcrito em [`docs/requisitos/`](docs/requisitos/README.md) — um arquivo por grupo
(`GER`, `LIV`, `CLI`, `VEN`, `EST`, `ANA`, `REC`). Leia o arquivo do grupo antes de implementar algo dele.

Duas aplicações em `apps/`: `apps/server` (back-end) e `apps/web` (front-end).
Decisões do projeto ficam em [`docs/decisoes/`](docs/decisoes/README.md), uma por arquivo (estilo MADR): escolhas de
tecnologia e regras de negócio que o DRS não define ou que o estendem. Consulte antes de implementar e registre lá
novas decisões.

Stack do `server`: Java 21, Spring Boot 4.1.x, Maven, Spring Data JPA, Spring Security (JWT), Hibernate Envers,
Lombok.
Bancos PostgreSQL e H2, escolhidos por profile (`h2` padrão, `postgres`). Sem migrations: schema gerado pelo ORM e
seed por scripts executados na inicialização. Arquitetura hexagonal por módulo, com uma regra/validação/operação por
arquivo; leia [`docs/arquitetura.md`](docs/arquitetura.md) antes de implementar no `server`.

Stack do `web`: Preact + Vite + TypeScript, com `preact-iso` (rotas), `uplot` (gráficos) e `openapi-fetch` (cliente
da API, com tipos gerados do OpenAPI do `server`), CSS puro e formulários nativos. Mantenha as dependências mínimas
(lista fechada em `docs/decisoes/DEC-01-stack-do-web.md`, ampliada por
`docs/decisoes/DEC-07-convencoes-da-api-rest.md`). E2E com Playwright usando o Chrome instalado
(`channel: 'chrome'`), sem baixar navegadores.

Comandos do `server` (rodar em `apps/server`):

```bash
mvn test                                    # testes (unitários, integração em H2 e ArchUnit)
mvn spring-boot:run                         # executa com H2 em memória (profile padrão)
docker compose up -d                        # Postgres local
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Variáveis do profile `postgres`: `DB_PORTA` (padrão `5432`, usada pelo Compose e pela aplicação; exporte, por
exemplo, `DB_PORTA=5433` se a 5432 estiver ocupada), `DB_USUARIO` e `DB_SENHA` (padrão `livraria`) e `DB_URL`
(sobrescreve a URL inteira).

Comandos do `web` (rodar em `apps/web`):

```bash
npm install                                 # instala as dependências (lista fechada em DEC-01 e DEC-07)
npm run dev                                 # Vite em http://localhost:5173, com proxy de /api e /v3 para o server
npm run build                               # checagem de tipos + build em apps/web/dist
npm run gerar-api                           # regenera src/api/esquema.d.ts a partir de openapi.json
npm run typecheck                           # só a checagem de tipos
```

O jar único da demonstração é montado rodando `npm run build` em `apps/web` e depois `mvn package` em
`apps/server` (o server copia `apps/web/dist` para `static/`, DEC-09).

## Escopo do MVP

O objetivo atual é um MVP. Prioridades:

- **Dentro:** cadastro de clientes (`CLI`: cadastro, alteração, inativação, consulta, endereços, cartões, senha),
  vitrine/consulta de livros (leitura), carrinho e fluxo de venda completo (`VEN`: carrinho com bloqueio por prazo,
  frete, pagamento com múltiplos cartões e cupons, status do pedido, trocas e cupom de troca), baixa/reentrada de
  estoque decorrentes de vendas e trocas (`RF-EST-003`, `RF-EST-004`, `RN-EST-006`), log de transação (`RNF-GER-002`),
  análise de vendas com gráfico e exportação (`ANA`) e recomendação/chatbot com IA generativa (`REC`).
- **Fora (por ora):** CRUD de livros (`RF-LIV-*` de escrita, ativação/inativação) e CRUD/entrada manual de estoque
  (`RF-EST-001`). Livros, estoque e tabelas de domínio (autor, editora, categoria, grupo de precificação, fornecedor,
  bandeiras de cartão etc.) são inseridos via **script SQL de seed** (atende `RNF-LIV-002`).
  As regras de cálculo de preço (`RF-EST-002`, `RN-EST-002`) ainda devem valer sobre os dados do seed.

Na dúvida se algo está no escopo, pergunte em vez de implementar.

## Ambiente

Não há nada em produção. Consequências:

- Não se preocupe com LGPD/GDPR, anonimização ou retenção de dados.
- Migrations não precisam de backfill nem de compatibilidade com dados existentes; pode-se editar/reescrever
  migrations e resetar o banco de desenvolvimento livremente.
- Dados de teste (inclusive cartões fictícios) podem ficar no seed.
- A operadora de cartão (`RN-VEN-007`) e o cálculo de frete (`RF-VEN-004`) podem ser simulados por um serviço fake
  com regras determinísticas.

## Convenções

- Idioma: domínio, requisitos e textos da UI em **português**; identificadores de código seguem a convenção da stack
  (preferência por nomes de domínio em português, consistentes com o DRS: `Cliente`, `Pedido`, `CupomTroca`...).
- Status de pedido exatamente como no DRS: `EM PROCESSAMENTO`, `APROVADA`, `REPROVADA`, `EM TRANSPORTE`, `ENTREGUE`,
  `EM TROCA`, `TROCA AUTORIZADA`, `TROCADO`. O pedido usa só os cinco primeiros; os de troca, mais `TROCA RECUSADA`,
  ficam na entidade `Troca` (`docs/decisoes/DEC-12-pedido-checkout-e-trocas.md`).
- Regras de negócio ficam na camada de domínio/serviço, não em controllers nem na UI; cada validação deve ser
  testável isoladamente.
- Valores monetários com tipo decimal exato (nunca float); formatação `R$ 1.234,56` na UI.
- Senhas com hash forte (bcrypt/argon2) — `RNF-CLI-003`.
- Referencie o ID do requisito em testes e, quando útil, em comentários (`// RN-VEN-004`).

## Rastreabilidade

Cada arquivo em `docs/requisitos/` tem uma **matriz de rastreabilidade**. Ao implementar (total ou parcialmente) um
requisito, atualize a linha correspondente: status (✅ / 🟡 / ❌), onde no código e observação. Não altere o texto dos
requisitos; se encontrar ambiguidade ou conflito no DRS, registre na coluna Observação e avise.

## Fluxo de trabalho

- Mudanças pequenas e focadas; rode testes antes de concluir.
- Não faça commit/push sem pedido explícito.
