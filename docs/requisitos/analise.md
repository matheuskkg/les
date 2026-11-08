# Análise

Prefixo do grupo: `ANA`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

| ID | Nome | Descrição |
|---|---|---|
| RF-ANA-001 | Analisar histórico de vendas | Administrador consulta o histórico de vendas por categoria de produto em um período (data de início e fim). |
| RF-ANA-002 | Filtrar período de análise | Administrador informa data de início e fim do gráfico gerencial; a data de fim não pode ser anterior à de início. |
| RF-ANA-003 | Selecionar categorias para comparação | Administrador seleciona uma ou mais categorias para exibição simultânea no gráfico de evolução de vendas. |
| RF-ANA-004 | Exportar dados do gráfico | Administrador exporta os dados do gráfico (período, categoria, valor) em planilha. |

## Requisitos não funcionais

| ID | Nome | Descrição |
|---|---|---|
| RNF-ANA-001 | Gráfico de linhas | Histórico em gráfico de linhas: eixo X = períodos (mês/ano); eixo Y = valor total em R$; uma linha de cor distinta por categoria, com legenda. |
| RNF-ANA-002 | Formatação monetária do eixo | Valores do eixo Y no formato monetário brasileiro (ex.: R$ 20.000,00), com escala automática conforme o maior valor do período. |
| RNF-ANA-003 | Legenda do gráfico | Legenda na parte inferior identificando o nome e a cor de cada categoria. |
| RNF-ANA-004 | Interatividade do gráfico | Ao posicionar o cursor sobre um ponto, exibir o valor exato de venda da categoria naquele período (tooltip). |

## Regras de negócio

| ID | Nome | Descrição |
|---|---|---|
| RN-ANA-001 | Granularidade do período | Agrupamento mensal: soma de todas as vendas aprovadas de cada categoria no mês. |
| RN-ANA-002 | Período mínimo e máximo | O intervalo entre início e fim deve ser de no mínimo 1 mês e no máximo 24 meses. |
| RN-ANA-003 | Categorias sem vendas no período | Mês sem vendas para uma categoria selecionada vale R$ 0,00, mantendo a linha contínua. |
| RN-ANA-004 | Status considerados no cálculo | Somente compras APROVADA, EM TRÂNSITO/TRANSPORTE ou ENTREGUE entram no cálculo; REPROVADA ou canceladas não. |

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RF-ANA-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/aplicacao/casodeuso/AnalisarVendas.java`, `apps/server/src/main/java/br/fatec/les/livraria/vendas/aplicacao/casodeuso/ConsultarItensVendidos.java`, `apps/server/src/main/java/br/fatec/les/livraria/analise/infraestrutura/web/AnaliseAdminController.java`, `apps/web/src/modulos/admin/paginas/AdminAnalise.tsx`, `apps/server/src/main/resources/seed/400_pedidos_historicos.sql` | Por categoria copiada no pedido; item com várias categorias entra com valor cheio em cada uma ([DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md)). As categorias vêm do snapshot JSON do livro e são agregadas em Java ([DEC-16](../decisoes/DEC-16-modelo-de-dados.md)). |
| RF-ANA-002 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/dominio/regra/FimNaoAnteriorAoInicio.java`, `apps/web/src/modulos/admin/regras/filtrosDaAnalise.ts` | Período informado em meses (`AAAA-MM`), com o inicial e o final inteiros. |
| RF-ANA-003 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/dominio/regra/CategoriaSelecionada.java`, `apps/web/src/modulos/admin/paginas/AdminAnalise.tsx` | Sem categoria na URL, a tela analisa todas; categoria inexistente responde 400. |
| RF-ANA-004 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/infraestrutura/web/PlanilhaDaAnalise.java`, `apps/server/src/main/java/br/fatec/les/livraria/analise/infraestrutura/web/AnaliseAdminController.java` | Só CSV (`mes`, `categoria`, `valor`; UTF-8 com BOM, `;`, vírgula decimal), gerado pelo `server` ([DEC-15.4](../decisoes/DEC-15.4-acesso-consultas-e-exportacao.md)). |
| RNF-ANA-001 | ✅ | `apps/web/src/modulos/admin/componentes/GraficoDeVendas.tsx` | uPlot; eixo X em `mm/aaaa`; cores da paleta categórica validada para daltonismo, fixas pela categoria. |
| RNF-ANA-002 | ✅ | `apps/web/src/modulos/admin/componentes/GraficoDeVendas.tsx`, `apps/web/src/compartilhado/formatacao.ts` |  |
| RNF-ANA-003 | ✅ | `apps/web/src/modulos/admin/componentes/GraficoDeVendas.tsx` |  |
| RNF-ANA-004 | ✅ | `apps/web/src/modulos/admin/componentes/GraficoDeVendas.tsx` | Tooltip com o valor de todas as categorias no mês sob o cursor; a tabela abaixo do gráfico repete os valores. |
| RN-ANA-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/dominio/modelo/AnaliseDeVendas.java`, `apps/server/src/main/java/br/fatec/les/livraria/vendas/aplicacao/consulta/ItemVendido.java` | Mês da finalização; preço unitário × quantidade não trocada, sem frete e sem rateio do desconto ([DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md)). |
| RN-ANA-002 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/dominio/regra/PeriodoDeAte24Meses.java` | Interpretação: o intervalo conta o mês inicial e o final, então vai de 1 mês (início = fim) a 24 meses (por exemplo, 11/2024 a 10/2026). |
| RN-ANA-003 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/analise/dominio/modelo/AnaliseDeVendas.java` |  |
| RN-ANA-004 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/vendas/dominio/pedido/StatusPedido.java`, `apps/server/src/main/java/br/fatec/les/livraria/vendas/infraestrutura/persistencia/VendasConsultasJpa.java` | Considera pedidos `APROVADA`, `EM TRANSPORTE` e `ENTREGUE` (mesmo conjunto do ranking). Desvio: desconta as quantidades de trocas concluídas; "canceladas" não corresponde a nenhum status do DRS ([DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md)). |
