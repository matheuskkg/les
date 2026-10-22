# DEC-05.1 — Prazo de bloqueio do carrinho

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-21
- **Card:** #12
- **Requisitos:** RN-VEN-014, RNF-VEN-001

## Contexto

RN-VEN-014 bloqueia os itens adicionados ao carrinho por um prazo "parametrizado", contado a partir do último item
incluído, e avisa o cliente 5 minutos antes de expirar. O DRS não define o prazo.

## Decisão

| Parâmetro | Padrão | Significado |
|---|---|---|
| `livraria.carrinho.prazo-bloqueio` | `30m` | Tempo do bloqueio, contado a partir do último item incluído no carrinho. |
| `livraria.carrinho.aviso-expiracao` | `5m` | Antecedência do aviso ao cliente (fixada pelo DRS em 5 minutos). |

Ambos são propriedades de configuração (`Duration`), sobrescrevíveis por profile ou variável de ambiente.

## Alternativas consideradas

- 15 minutos: apertado para uma compra real.
- 60 minutos: dificulta demonstrar a expiração sem mudar o parâmetro.

## Consequências

- Para demonstrar o aviso e a expiração, reduz-se o prazo por configuração (por exemplo, `prazo-bloqueio=6m`).
