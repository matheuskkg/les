# Registros de decisão

Decisões do projeto, uma por arquivo, no estilo [MADR](https://adr.github.io/madr/) (*Markdown Any Decision
Records*, variante do ADR que serve também para decisões de negócio). O nome do arquivo usa o ID do card do board
(`DEC-NN-titulo.md`); quando um card reúne decisões independentes, cada uma ganha um subnúmero (`DEC-NN.N-titulo.md`).

Escopos:

- **Tecnologia:** stack, bibliotecas e ferramentas.
- **Negócio:** regras que o DRS não define ou que estende.

### Tecnologia

| ID | Decisão | Status |
|---|---|---|
| [DEC-01](DEC-01-stack-do-web.md) | Stack do web | Aceita |
| [DEC-02](DEC-02-framework-e2e.md) | Framework de testes E2E | Aceita |

### Negócio

| ID | Decisão | Status |
|---|---|---|
| [DEC-04](DEC-04-ranking-de-cliente.md) | Ranking de cliente | Aceita |
| [DEC-05.1](DEC-05.1-bloqueio-do-carrinho.md) | Prazo de bloqueio do carrinho | Aceita |
| [DEC-05.2](DEC-05.2-operadora-de-cartao.md) | Operadora de cartão simulada | Aceita |
| [DEC-05.3](DEC-05.3-frete.md) | Cálculo de frete simulado | Aceita |
| [DEC-06](DEC-06-validade-do-cartao.md) | Data de validade no cartão de crédito | Aceita |

## Modelo

```markdown
# DEC-NN — Título

- **Status:** Proposta | Aceita | Substituída por DEC-NN
- **Escopo:** Tecnologia | Negócio
- **Data:** AAAA-MM-DD
- **Card:** #NN
- **Requisitos:** RF-..., RN-...

## Contexto

O problema e o que o DRS diz (ou deixa de dizer).

## Decisão

O que foi decidido, com detalhe suficiente para implementar e testar.

## Alternativas consideradas

- Opção — por que não.

## Consequências

O que muda no código, no seed, nos testes ou em outros requisitos.
```

Uma decisão aceita não é reescrita: se mudar, crie outra e marque a antiga como substituída.
