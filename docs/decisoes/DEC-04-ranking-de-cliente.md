# DEC-04 — Ranking de cliente

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-21
- **Card:** #11
- **Requisitos:** RN-CLI-007

## Contexto

RN-CLI-007 pede um "ranking numérico com base no seu perfil de compra", sem definir a fórmula.

## Decisão

RFM simplificado: ranking inteiro de **0 a 100**.

**Pedidos considerados:** do próprio cliente, com status `APROVADA`, `EM TRANSPORTE`, `ENTREGUE`, `EM TROCA` ou
`TROCA AUTORIZADA`, feitos nos **últimos 12 meses** (365 dias até a data do cálculo). Ficam de fora
`EM PROCESSAMENTO`, `REPROVADA` e `TROCADO` (o valor trocado volta ao cliente como cupom). (alterado pelo
[DEC-12](DEC-12-pedido-checkout-e-trocas.md): o pedido não tem mais status de troca; contam `APROVADA`,
`EM TRANSPORTE` e `ENTREGUE`.)

**Fatores** (cada um vira uma nota de 0 a 100 por faixa):

| Nota | Valor (V): soma dos totais dos pedidos | Frequência (F): nº de pedidos | Recência (R): dias desde o último pedido |
|---:|---|---|---|
| 0 | R$ 0,00 | 0 | sem pedido na janela |
| 20 | até R$ 100,00 | 1 | 271 a 365 |
| 40 | até R$ 300,00 | 2 | 181 a 270 |
| 60 | até R$ 600,00 | 3 ou 4 | 91 a 180 |
| 80 | até R$ 1.000,00 | 5 a 7 | 31 a 90 |
| 100 | acima de R$ 1.000,00 | 8 ou mais | 0 a 30 |

Os limites das faixas de valor são inclusivos ("até R$ 100,00" inclui R$ 100,00). O valor do pedido é o total da
compra (itens + frete), em `BigDecimal` (alterado pelo [DEC-12](DEC-12-pedido-checkout-e-trocas.md): menos os cupons
gerados pelas trocas concluídas do pedido).

**Fórmula:** `ranking = 0,5·V + 0,3·F + 0,2·R`, arredondado para inteiro com `HALF_UP`. Cliente sem pedidos
considerados tem ranking **0**.

**Cálculo sob demanda:** o ranking não é persistido; é calculado ao consultar o cliente (perfil e consulta do
administrador, CLI-07).

Exemplo: 3 pedidos somando R$ 450,00, o último há 20 dias → V = 60, F = 60, R = 100 →
0,5·60 + 0,3·60 + 0,2·100 = 68.

## Alternativas consideradas

- 1 ponto por R$ 1,00 gasto: simples, mas ignora frequência e recência e cresce sem teto.
- Níveis de 1 a 5 por faixas de gasto e pedidos: menos granular.

## Consequências

- Como é calculado na consulta, a recência fica sempre correta sem job de recálculo; em compensação, ordenar a
  consulta de clientes por ranking exige calcular para todos os filtrados.
- Cada faixa e a fórmula são testáveis isoladamente (uma regra por arquivo).
