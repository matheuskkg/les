# DEC-15.1 — Arredondamento

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-23
- **Card:** #72
- **Requisitos:** RF-EST-002, RN-EST-002, RN-CLI-007, RF-VEN-004, RF-VEN-007

## Contexto

O `Dinheiro` arredondava com `HALF_EVEN`, enquanto o ranking ([DEC-04](DEC-04-ranking-de-cliente.md)), o frete
([DEC-05.3](DEC-05.3-frete.md)) e o cupom percentual ([DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md)) usam
`HALF_UP`. O arredondamento do preço de venda (RF-EST-002) não estava definido.

## Decisão

- Todo arredondamento usa **`HALF_UP`**. O `Dinheiro` tem escala 2 e `HALF_UP`.
- Arredonda-se **uma vez, no fim do cálculo**; os passos intermediários ficam em `BigDecimal` sem arredondar.
  - Preço de venda: maior custo entre as entradas × (1 + margem do grupo de precificação), e só o resultado vai a 2
    casas (RF-EST-002, RN-EST-002).
  - Desconto percentual: subtotal dos itens × percentual, arredondado no fim.
- O ranking, que não é dinheiro, é arredondado para inteiro com `HALF_UP` ([DEC-04](DEC-04-ranking-de-cliente.md)).

Exemplo: custo R$ 33,33 com margem de 15% → 38,3295 → R$ 38,33.

## Alternativas consideradas

- `HALF_EVEN`: evita viés ao somar muitos valores arredondados, o que não acontece aqui, porque cada valor é
  arredondado uma vez. Também foge do arredondamento comercial que o cliente espera.
- Arredondar cada passo: acumula diferenças de centavos.

## Consequências

- O `Dinheiro` passa a `HALF_UP`, e o teste dele cobre o meio-centavo (`10,005` → `10,01`).
- As regras de preço, frete e desconto calculam em `BigDecimal` e convertem para `Dinheiro` só no resultado.
