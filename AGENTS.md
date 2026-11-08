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

Stack do `web`: Preact + Vite + TypeScript, CSS puro e formulários nativos, com a lista fechada de dependências do
DEC-01 (ampliada pelo DEC-07); leia [`DEC-17`](docs/decisoes/DEC-17-organizacao-do-web.md) antes de implementar no
`web`. E2E com Playwright usando o Chrome instalado (`channel: 'chrome'`), sem baixar navegadores.

Comandos do `server` (rodar em `apps/server`; o profile `postgres` usa o `docker-compose.yml` e as variáveis de
`application-postgres.yml`):

```bash
mvn test                                    # testes (unitários, integração em H2 e ArchUnit)
mvn spring-boot:run                         # executa com H2 em memória (profile padrão)
```

No `web` (`apps/web`), os scripts estão no `package.json`; `npm run e2e` sobe o server (h2,e2e) e o Vite sozinho.

Ao mudar a API, grave o contrato e regenere os tipos do `web`:

```bash
mvn test -Dtest=ContratoOpenApiTest -Dlivraria.openapi.gravar=true   # em apps/server: atualiza apps/web/openapi.json
npm run gerar-api                                                      # em apps/web: atualiza src/api/esquema.d.ts
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
- O schema e o seed podem mudar sem compatibilidade com dados existentes; o banco pode ser resetado livremente.
- Dados de teste (inclusive cartões fictícios) podem ficar no seed.
- A operadora de cartão (`RN-VEN-007`) e o cálculo de frete (`RF-VEN-004`) podem ser simulados por um serviço fake
  com regras determinísticas.

## Convenções

- Idioma: domínio, requisitos e textos da UI em **português**; identificadores de código seguem a convenção da stack
  (preferência por nomes de domínio em português, consistentes com o DRS: `Cliente`, `Pedido`, `CupomTroca`...).
- Status de pedido e de troca exatamente como no DRS, divididos entre `Pedido` e `Troca` conforme o
  [`DEC-12`](docs/decisoes/DEC-12-pedido-checkout-e-trocas.md).
- Regras de negócio ficam na camada de domínio/serviço, não em controllers nem na UI; cada validação deve ser
  testável isoladamente.
- Valores monetários com tipo decimal exato (nunca float); formatação `R$ 1.234,56` na UI.
- Senhas com hash forte (bcrypt/argon2) — `RNF-CLI-003`.
- Documentos formais (entregas em `docs/entregas/`, que viram .docx): escreva e revise com a skill
  [`humanizar-texto`](.claude/skills/humanizar-texto/SKILL.md); ao delegar a escrita, repasse as regras dela no prompt.
- Referencie o ID do requisito em testes e, quando útil, em comentários (`// RN-VEN-004`).

## Testes E2E

Os E2E existem **somente para a apresentação** ao professor, não como suíte de regressão.

- Todo roteiro fica em `apps/web/e2e/apresentacao<Fluxo>.spec.ts`; `fixtures.ts` é o apoio compartilhado.
- Há só um comando: `npm run e2e` roda todos os roteiros; `npm run e2e -- e2e/apresentacaoAnalise.spec.ts` roda um
  fluxo específico.
- Não crie um script `e2e:*` por fluxo no `package.json`.
- Fora do CI, o Chrome abre visível e cada ação espera 500 ms (`ESPERA_ENTRE_ACOES_MS`); no CI roda headless e sem
  atraso.
- Contra o jar empacotado: `npm run e2e -- -c playwright.apresentacao.config.ts`.
- O relatório é aberto com `npx playwright show-report`.
- Antes de criar ou alterar um roteiro, siga a skill [`e2e-testes`](.claude/skills/e2e-testes/SKILL.md).

## Rastreabilidade

Cada arquivo em `docs/requisitos/` tem uma **matriz de rastreabilidade**. Ao implementar (total ou parcialmente) um
requisito, atualize a linha correspondente: status (✅ / 🟡 / ❌), onde no código e observação. Não altere o texto dos
requisitos; se encontrar ambiguidade ou conflito no DRS, registre na coluna Observação e avise.

## Fluxo de trabalho

- Mudanças pequenas e focadas; rode testes antes de concluir.
- Não faça commit/push sem pedido explícito.
