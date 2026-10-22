# DEC-05.3 — Cálculo de frete simulado

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-21
- **Card:** #12
- **Requisitos:** RF-VEN-004

## Contexto

RF-VEN-004 pede o frete calculado com base nos itens selecionados e no endereço de entrega. O cálculo é simulado por
um serviço fake com regras determinísticas (ver `AGENTS.md`).

## Decisão

Calculado a partir do **CEP do endereço de entrega** e dos **itens do carrinho** (dimensões e peso do livro,
RN-LIV-001).

1. **Peso considerado** de cada item: o maior entre o peso real (kg) e o peso cúbico
   `altura × largura × profundidade (cm) / 6000`; multiplica-se pela quantidade e soma-se tudo.
2. **Blocos de peso:** número de blocos iniciados de 0,5 kg (`ceil(peso / 0,5)`, mínimo 1).
3. **Frete** = base da região + R$ 2,00 × (blocos − 1).

Região pelo primeiro dígito do CEP:

| 1º dígito | Estados | Base |
|---|---|---:|
| 0, 1 | SP | R$ 12,00 |
| 2, 3 | RJ, ES, MG | R$ 15,00 |
| 8, 9 | PR, SC, RS | R$ 18,00 |
| 7 | DF, GO, TO, MT, MS, RO | R$ 22,00 |
| 4, 5 | BA, SE, PE, AL, PB, RN | R$ 25,00 |
| 6 | CE, PI, MA, PA, AM, AC, AP, RR | R$ 30,00 |

Cálculos em `BigDecimal`, resultado arredondado a 2 casas com `HALF_UP`. Carrinho vazio não tem frete.

Exemplo: 2 livros de 0,4 kg (peso cúbico 0,3 kg) para CEP `01310-100` → 0,8 kg → 2 blocos →
R$ 12,00 + R$ 2,00 = R$ 14,00.

## Alternativas consideradas

- Frete fixo ou só por região: não atende "com base nos itens selecionados" de RF-VEN-004.

## Consequências

- O frete depende de peso e dimensões dos livros, já obrigatórios por RN-LIV-001.
