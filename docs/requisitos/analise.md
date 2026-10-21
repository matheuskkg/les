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
| RF-ANA-001 | ❌ |  |  |
| RF-ANA-002 | ❌ |  |  |
| RF-ANA-003 | ❌ |  |  |
| RF-ANA-004 | ❌ |  |  |
| RNF-ANA-001 | ❌ |  |  |
| RNF-ANA-002 | ❌ |  |  |
| RNF-ANA-003 | ❌ |  |  |
| RNF-ANA-004 | ❌ |  |  |
| RN-ANA-001 | ❌ |  |  |
| RN-ANA-002 | ❌ |  |  |
| RN-ANA-003 | ❌ |  |  |
| RN-ANA-004 | ❌ |  |  |
