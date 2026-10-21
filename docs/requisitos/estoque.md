# Controle de Estoque

Prefixo do grupo: `EST`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

| ID | Nome | Descrição |
|---|---|---|
| RF-EST-001 | Realizar entrada em estoque | Permitir entrada de itens de livros em estoque, indicando o livro previamente cadastrado e a quantidade. |
| RF-EST-002 | Calcular valor de venda | Valor de venda = valor de custo + percentual do grupo de precificação do livro. |
| RF-EST-003 | Dar baixa em estoque | Para cada venda realizada deve-se dar baixa no estoque do total de itens vendidos. |
| RF-EST-004 | Realizar reentrada em estoque | O sistema deve realizar a reentrada de um item em estoque a partir da troca de um produto. |

## Requisitos não funcionais

_Nenhum neste grupo._

## Regras de negócio

| ID | Nome | Descrição |
|---|---|---|
| RN-EST-001 | Validar dados de estoque | Toda entrada deve informar produto, quantidade, valor de custo, fornecedor e data de entrada. |
| RN-EST-002 | Valor de item com diferentes custos | Com custos diferentes para o mesmo livro, o valor de venda (igual para todos os itens) é calculado pelo maior custo. |
| RN-EST-003 | Quantidade de itens | Não permitir entrada de itens com quantidade igual a zero. |
| RN-EST-004 | Valor de custo | Para todo item deve haver um valor de custo. |
| RN-EST-005 | Data de entrada | Não permitir registrar itens sem data de entrada. |
| RN-EST-006 | Validar retorno da operadora de cartão de crédito | Somente deve-se dar baixa no estoque de itens cuja compra tenha sido efetivada (status diferente de EM PROCESSAMENTO). Todo item de uma compra não aprovada deve ser desbloqueado e mantido em estoque. |

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RF-EST-001 | ❌ |  |  |
| RF-EST-002 | ❌ |  |  |
| RF-EST-003 | ❌ |  |  |
| RF-EST-004 | ❌ |  |  |
| RN-EST-001 | ❌ |  |  |
| RN-EST-002 | ❌ |  |  |
| RN-EST-003 | ❌ |  |  |
| RN-EST-004 | ❌ |  |  |
| RN-EST-005 | ❌ |  |  |
| RN-EST-006 | ❌ |  |  |
