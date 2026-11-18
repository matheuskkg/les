---
name: code-review
description: Revisa mudanças de código lendo primeiro as instruções específicas do app alterado (web ou server), em vez de aplicar um checklist genérico. Use sempre que o usuário pedir code review, revisão de diff/PR, "olha essa mudança antes de eu commitar", avaliar se algo respeita a arquitetura, os DECs e os requisitos, ou revisar arquivos em apps/web, apps/server, testes E2E e documentos de entrega.
---

# Code review da Livraria

Uma revisão boa aqui começa antes de olhar o código: o projeto tem regras escritas e elas mudam de lugar conforme o
app. No `server` valem a arquitetura hexagonal por módulo, os DECs de negócio e os requisitos do grupo; no `web` valem
a organização por funcionalidade, a lista fechada de dependências e as regras de dependência verificadas pelo
`arquitetura.test.ts`. Em documentos formais, valem as regras do próprio `docs/entregas/dvp/AGENTS.md`. Opinar sem
ler esses arquivos produz revisão genérica: aponta problema que não existe e deixa passar o que o projeto decidiu.

Esta skill não repete as regras. Ela manda ler a fonte, no arquivo certo, e julgar o diff contra ela. Se um resumo
aqui divergir do documento canônico, o documento canônico ganha.

## Quando usar

- Ao pedirem "revisa esse diff", "faz um code review", "olha antes de eu commitar" ou "pode mergear?".
- Quando a mudança toca `apps/server`, `apps/web`, contrato OpenAPI, E2E ou `docs/entregas/`.
- Quando o pedido cita um requisito (`RF-VEN-...`), um DEC ou o escopo do MVP e pergunta se o código está de acordo.
- Antes de aceitar código escrito por outro agente.

## Como revisar

1. **Descubra o escopo.** Se o usuário colou um diff ou indicou arquivos, esse é o escopo: não rode `git diff` para
   aumentá-lo, porque o working tree pode conter mudanças alheias ao pedido. Se não indicou nada, rode
   `git status --short` e `git diff` na raiz do repo, incluindo o que está staged (`git diff --cached`). Diff vazio
   não é licença para revisar qualquer coisa: diga que não há o que revisar. Não altere, commite nem faça stash de
   nada.
2. **Classifique cada arquivo do diff** em `server` (`apps/server/`), `web` (`apps/web/`), `entregas`
   (`docs/entregas/`), documentação de referência (`docs/requisitos/`, `docs/decisoes/`, `AGENTS.md`) ou `outro`.
   Um diff pode tocar mais de um grupo: nesse caso, siga todos os caminhos aplicáveis.
3. **Leia sempre o `AGENTS.md` da raiz.** É onde estão as convenções comuns e, sobretudo, o que ficou fora do MVP.
4. **Leia o roteiro do app antes de opinar**, conforme a tabela abaixo. Os arquivos em `references/` são roteiros
   para os documentos canônicos, não substitutos deles: abra também os documentos apontados.
5. **Se o diff muda a API**, leia `DEC-07` e confira se o contrato e os tipos do `web` acompanharam
   (`ContratoOpenApiTest` com `-Dlivraria.openapi.gravar=true` e `npm run gerar-api`).
6. **Acione e aplique as skills existentes quando o diff entra no território delas.** Carregue `e2e-testes` se houver
   spec em `apps/web/e2e/` e `humanizar-texto` se houver prosa em `docs/entregas/`. Não basta citá-las: revise o trecho
   com as regras delas e relate quais marcas ou anti-padrões você encontrou. Não reescreva as skills aqui.
7. **Escreva o relatório** no formato abaixo, na conversa. Nunca edite os arquivos revisados.

### Roteiro por caminho

| Caminho alterado | Leia antes de revisar |
|---|---|
| `apps/server/**` | `references/server.md` → `docs/arquitetura.md`, convenções do `AGENTS.md` da raiz e requisitos/DECs do módulo tocado |
| `apps/web/**` (exceto `e2e/`) | `references/web.md` → `DEC-17`, `DEC-01`, `DEC-07` quando a mudança consome ou altera a API, e `src/arquitetura.test.ts` |
| `apps/web/e2e/**` | `references/web.md` + skill `e2e-testes` (é roteiro de apresentação, não suíte de regressão) |
| `docs/entregas/**` | `references/entregas.md` → `docs/entregas/dvp/AGENTS.md` + skill `humanizar-texto` para a prosa |
| `docs/requisitos/**`, `docs/decisoes/**`, `AGENTS.md` | índice `docs/requisitos/README.md` ou `docs/decisoes/README.md`, conforme o caso; confira se a matriz de rastreabilidade acompanhou o código |

O módulo do `server` aponta o arquivo de requisitos: `clientes` → `clientes.md`, `catalogo` → `livros.md`,
`vendas` → `vendas.md`, `estoque` → `estoque.md`, `analise` → `analise.md`, `recomendacao` → `recomendacao.md`,
`acesso` e `compartilhado` → `geral.md`. Para achar o DEC aplicável, comece pelo índice `docs/decisoes/README.md`,
que separa Tecnologia e Negócio.

## Regras da revisão

- **Julgue contra a fonte, não contra lembrança.** Antes de afirmar que algo fere a arquitetura, abra o documento e
  cite o ID do requisito ou DEC. "Acho que o DEC diz..." não é achado.
- **Separe bloqueante de sugestão.** Bloqueante é violação de regra escrita, requisito descumprido, regra de negócio
  no lugar errado, dado em `double`, teste ausente para regra nova. Sugestão é legibilidade, nome melhor, código
  repetido que ainda funciona. Pergunta é o que a instrução não responde e precisa de decisão.
- **Fora do MVP não é defeito.** CRUD de livros e entrada manual de estoque estão de fora por enquanto; não os
  aponte como falta. Vale o que está dentro: cadastro de cliente, vitrine, carrinho, venda, estoque decorrente de
  venda e troca, análise e recomendação.
- **Regra de negócio fica no domínio/serviço, nunca em controller ou na UI.** Se o diff puser validação de negócio
  no controller, no componente ou no hook, é bloqueante com ID.
- **Um arquivo por regra, validação e operação** no `server`: regra pura em `dominio/regra` acumula `Violacoes`;
  regra que consulta dados vai em `aplicacao/regra`; caso de uso é uma classe com `executar(Comando)` e
  `@Transactional`.
- **Rastreabilidade acompanha o código.** Se o requisito foi implementado ou mudou, a matriz em
  `docs/requisitos/<grupo>.md` deve refletir. Confira a linha quando o diff toca regra identificada por ID.
- **Não reescreva a regra para "limpar" o código.** Se o comportamento do diff diverge do DRS ou de um DEC, registre
  a divergência com ID; o sistema continua como é até alguém decidir mudar.
- **Teste é parte da entrega.** Toda regra nova pede teste isolado que a cite pelo ID. Verifique também se os testes
  existentes foram adaptados, não só se compilam.
- **Se você não leu o documento, diga.** Melhor uma revisão curta e fundada do que um palpite longo.

## Relatório

Use este formato, em português:

```markdown
## Revisão — <escopo>

**Instruções lidas:** <arquivos do roteiro e documentos canônicos abertos, por app>

### Bloqueantes
1. `<caminho>:<linha>` — <o que viola qual regra> (`ID`). Correção: <o que fazer>.

### Sugestões
- `<caminho>:<linha>` — <melhoria>.

### Perguntas
- <o que a instrução não responde e precisa de decisão>.

### Verificações
- <testes e comandos executados, com resultado>
```

Se não houver achados em uma seção, omita-a. "Instruções lidas" é obrigatório mesmo em diff pequeno: é o que mostra
que a revisão foi específica do app.

## Anti-padrões

- Revisar `apps/server` só com checklist do `web`, ou citar `DEC-17` para falar de persistência.
- Tratar o `AGENTS.md` da raiz como checklist completo e pular o documento do módulo tocado.
- Sugerir biblioteca nova no `web` sem justificativa: a lista do `DEC-01` é fechada.
- Pedir `double`/`float` para dinheiro, ou formatação de moeda no back-end: use `Dinheiro`/`BigDecimal`, com
  `R$ 1.234,56` só na UI.
- Apontar "falta CRUD de livros" ou "falta entrada manual de estoque" como problema: fora do MVP.
- Propor mover regra de negócio para o componente ou para o controller "para simplificar".
- Revisar E2E com critério de suíte de regressão (exigir `data-testid`, checar `disabled`): use `e2e-testes`.
- Corrigir texto de entrega no próprio diff sem passar por `humanizar-texto`.
- Escrever "de acordo com as boas práticas" sem ID.

## Antes de dar como pronto

- [ ] O escopo veio do diff ou do pedido explícito e nada foi modificado no repo.
- [ ] O `AGENTS.md` da raiz foi lido.
- [ ] Para cada app tocado, o `references/` correspondente **e** os documentos canônicos apontados por ele foram
      lidos.
- [ ] Cada bloqueante cita arquivo/linha e o ID do requisito ou DEC, ou diz explicitamente que não há ID.
- [ ] Achados foram separados em bloqueante, sugestão e pergunta.
- [ ] Fora do MVP não foi tratado como defeito.
- [ ] Specs E2E e entregas formais passaram por `e2e-testes` e `humanizar-texto`, quando aplicável.
- [ ] O relatório lista as instruções lidas.
