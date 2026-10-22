# DEC-05.2 — Operadora de cartão simulada

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-21
- **Card:** #12
- **Requisitos:** RN-VEN-007, RN-VEN-008

## Contexto

RN-VEN-007 exige o aceite da operadora de cartão na finalização da compra. A operadora é simulada por um serviço fake
com regras determinísticas (ver `AGENTS.md`).

## Decisão

Cada cartão do pagamento é avaliado separadamente; a compra é aprovada só se todos os cartões forem aceitos.

| Condição (avaliada nesta ordem) | Resposta |
|---|---|
| Cartão vencido na data da transação ([DEC-06](DEC-06-validade-do-cartao.md)) | Reprovado: "Cartão vencido" |
| Número do cartão termina em `0000` | Reprovado: "Transação recusada pela operadora" |
| Código de segurança é `000` | Reprovado: "Código de segurança inválido" |
| Qualquer outro caso | Aprovado |

## Alternativas consideradas

- Aprovação aleatória: não é determinística, inviabiliza testes.

## Consequências

- As reprovações são demonstráveis com cartões de teste que atendam às condições acima.
