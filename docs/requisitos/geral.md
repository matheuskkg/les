# Geral

Prefixo do grupo: `GER`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

_Nenhum neste grupo._

## Requisitos não funcionais

| ID | Nome | Descrição |
|---|---|---|
| RNF-GER-001 | Tempo de resposta para consultas | Toda consulta de usuário deve ter resposta em no máximo 1 segundo. |
| RNF-GER-002 | Log de transação | Para toda operação de escrita (inserção ou alteração) deve ser registrado data, hora, usuário responsável, além de manter os dados alterados. |

## Regras de negócio

_Nenhum neste grupo._

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RNF-GER-001 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/aplicacao/Paginacao.java`, `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/web/ResolvedorDePaginacao.java`, `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/persistencia/Especificacoes.java` | Consultas sempre paginadas, com no máximo 100 itens por página ([DEC-07](../decisoes/DEC-07-convencoes-da-api-rest.md)). Índices declarados com `@Index` nas colunas filtradas/ordenadas e FKs consultadas ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
| RNF-GER-002 | ❌ |  | A revisão do Envers guarda `usuarioId` e `usuario` (e-mail, `anonimo` ou `sistema`) ([DEC-08](../decisoes/DEC-08-autenticacao-e-sessao.md)). Finalização em duas transações para registrar `EM PROCESSAMENTO` ([DEC-13](../decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)); registros inseridos pelo seed não têm revisão ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
