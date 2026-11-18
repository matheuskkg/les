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
| [DEC-03](DEC-03-recomendacao-com-ia-generativa.md) | Recomendação e chatbot com IA generativa | Aceita |
| [DEC-07](DEC-07-convencoes-da-api-rest.md) | Convenções da API REST | Aceita |
| [DEC-08](DEC-08-autenticacao-e-sessao.md) | Autenticação, sessão e modelo de usuário | Aceita |
| [DEC-09](DEC-09-topologia-de-execucao.md) | Topologia de execução de web e server | Aceita |
| [DEC-13](DEC-13-processamento-do-pedido-e-notificacoes.md) | Processamento do pedido e notificações | Aceita |
| [DEC-14](DEC-14-ids-nomes-e-seed.md) | IDs, nomes e seed | Aceita |
| [DEC-15.3](DEC-15.3-saldo-de-estoque.md) | Saldo de estoque | Aceita |
| [DEC-16](DEC-16-modelo-de-dados.md) | Modelo de dados | Aceita |
| [DEC-17](DEC-17-organizacao-do-web.md) | Organização do código do web | Aceita |

### Negócio

| ID | Decisão | Status |
|---|---|---|
| [DEC-04](DEC-04-ranking-de-cliente.md) | Ranking de cliente | Aceita |
| [DEC-05.1](DEC-05.1-bloqueio-do-carrinho.md) | Prazo de bloqueio do carrinho | Aceita |
| [DEC-05.2](DEC-05.2-operadora-de-cartao.md) | Operadora de cartão simulada | Aceita |
| [DEC-05.3](DEC-05.3-frete.md) | Cálculo de frete simulado | Aceita |
| [DEC-06](DEC-06-validade-do-cartao.md) | Data de validade no cartão de crédito | Aceita |
| [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md) | Modelo de cliente, endereços e cartões | Aceita |
| [DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md) | Cupons e distribuição do pagamento | Aceita |
| [DEC-12](DEC-12-pedido-checkout-e-trocas.md) | Pedido, checkout e trocas | Aceita |
| [DEC-15.1](DEC-15.1-arredondamento.md) | Arredondamento | Aceita |
| [DEC-15.2](DEC-15.2-uso-do-cupom-promocional.md) | Uso do cupom promocional | Aceita |
| [DEC-15.4](DEC-15.4-acesso-consultas-e-exportacao.md) | Ajustes de acesso, consultas e exportação | Aceita |
| [DEC-18](DEC-18-bandeira-identificada-pelo-numero.md) | Bandeira do cartão identificada pelo número | Aceita |
| [DEC-19](DEC-19-filtro-da-analise-por-datas.md) | Filtro da análise de vendas por datas exatas | Aceita |

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
