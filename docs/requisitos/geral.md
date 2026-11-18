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
| RNF-GER-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/aplicacao/Paginacao.java`, `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/web/ResolvedorDePaginacao.java`, `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/persistencia/Especificacoes.java`, `apps/server/src/test/java/br/fatec/les/livraria/TempoDeRespostaTest.java`, `apps/web/e2e/apresentacaoAnalise.spec.ts`, `apps/web/src/modulos/admin/regras/paginacao.ts`, `apps/web/src/ui/Paginacao.tsx`, `apps/web/src/ui/regras/pagina.ts`, `apps/web/src/estilos/ui.css`, `apps/web/src/compartilhado/url.ts` | Consultas sempre paginadas, com no máximo 100 itens por página ([DEC-07](../decisoes/DEC-07-convencoes-da-api-rest.md)). Índices declarados com `@Index` nas colunas filtradas/ordenadas e FKs consultadas ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). O `TempoDeRespostaTest` mede vitrine, clientes, pedidos, trocas, análise e exportação com o seed completo (pedidos históricos do ANA-01) e exige no máximo 1 s; medido no back-end (o tempo de rede e de renderização no navegador não entra). O E2E de apresentação mede a consulta da análise a partir do clique em "Atualizar". Nas consultas do admin, o tamanho padrão passou a ser 10 e o seletor oferece 10, 20, 50 e 100 — todos dentro do limite do contrato —, mantendo filtros e ordem na URL ao trocar página ou tamanho. A página desejada é digitada em um campo de texto com máscara de dígitos (`inputMode="numeric"`, `pattern="[0-9]*"` e sanitização no evento, inclusive na colagem), confirmado por Enter ou ao sair do campo; campo vazio volta à página atual, zeros à esquerda são removidos e valores acima do total vão para a última página. O campo e o seletor de itens por página usam altura e largura compactas (ALT-09). A mesma preservação vale ao abrir um detalhe e voltar: a tela de origem viaja em `de` na URL do detalhe, com leitura e escrita centralizadas em `apps/web/src/compartilhado/url.ts`. |
| RNF-GER-002 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/auditoria/RevisaoJpa.java`, `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/auditoria/RevisaoListener.java` | A revisão do Envers guarda `usuarioId` e `usuario` (e-mail, `anonimo` ou `sistema`) ([DEC-08](../decisoes/DEC-08-autenticacao-e-sessao.md)). Finalização em duas transações para registrar `EM PROCESSAMENTO` ([DEC-13](../decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)); registros inseridos pelo seed não têm revisão ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
