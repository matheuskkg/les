# DEC-15.2 — Uso do cupom promocional

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-23
- **Card:** #72
- **Requisitos:** RN-VEN-003, RN-VEN-007, RF-VEN-007

## Contexto

O [DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md) guarda promocionais e cupons de troca numa tabela só, com
uma situação (disponível/usado) e bloqueio otimista. Mas o promocional vale uma vez **por cliente**, sem limite
global: marcar a linha dele como usada a bloquearia para todos, e dois clientes usando o mesmo código ao mesmo tempo
receberiam 409 um do outro.

## Decisão

O promocional e o cupom de troca ficam em tabelas separadas, e o uso do promocional é registrado por cliente.

| Tabela | Conteúdo |
|---|---|
| `cupom_promocional` | código, tipo, valor, validade e valor mínimo, criados pelo seed. Não tem situação |
| `uso_cupom_promocional` | promocional, cliente e pedido, com `UNIQUE (cupom_promocional_id, cliente_id)` |
| `cupom` | só cupons de troca: cliente, código, valor, validade, situação e `@Version` |

- O cliente pode usar um promocional se não houver uso dele registrado.
- `RegistrarPedido` grava o uso, e `ReprovarPedido` o apaga, devolvendo o promocional ao cliente
  ([DEC-13](DEC-13-processamento-do-pedido-e-notificacoes.md)).
- **Concorrência:** de duas finalizações simultâneas do mesmo cliente com o mesmo promocional, o `UNIQUE` recusa o
  segundo uso, e a API responde 409 ([DEC-07](DEC-07-convencoes-da-api-rest.md)). Clientes diferentes não disputam
  a mesma linha. Como a regra não depende do tempo, a restrição fica no banco.
- O cupom de troca segue como no DEC-11: usado ao finalizar, com `@Version`, e devolvido na reprovação.

As demais regras do DEC-11 (tipos, base do desconto, ordem da distribuição, cupom desnecessário) não mudam.

## Alternativas consideradas

- Situação na própria linha do promocional: bloqueia o cupom para todos depois do primeiro uso.
- Criar um cupom na tabela `cupom` para o cliente a cada uso: a linha só nasceria já usada, e a situação dela não
  teria função; na reprovação seria preciso apagá-la de qualquer jeito.
- Contar os pedidos do cliente com o código, sem tabela de uso: não há onde pôr a restrição de unicidade, e duas
  finalizações simultâneas passariam.

## Consequências

- A violação do `UNIQUE` vira 409 no `TratadorDeErros`, como o conflito de bloqueio otimista.
- O pedido continua copiando código, categoria, tipo e valor abatido de cada cupom
  ([DEC-12](DEC-12-pedido-checkout-e-trocas.md)).
- O seed `3NN` ([DEC-14](DEC-14-ids-nomes-e-seed.md)) insere os promocionais em `cupom_promocional` e os cupons de
  troca em `cupom`. Usos de promocional dos pedidos históricos entram no `4NN`.
