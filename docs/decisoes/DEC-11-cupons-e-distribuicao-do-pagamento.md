# DEC-11 — Cupons e distribuição do pagamento

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-22
- **Card:** #64
- **Requisitos:** RF-VEN-006, RF-VEN-007, RF-VEN-015, RN-VEN-003 a RN-VEN-007

## Contexto

O DRS permite pagar com cupons promocionais, cupons de troca e cartões (RF-VEN-007), limita a um promocional por
compra (RN-VEN-003), fixa R$ 10,00 por cartão com a exceção dos cupons (RN-VEN-004, RN-VEN-005) e manda gerar cupom de
troca pelo excedente sem aceitar cupons desnecessários (RN-VEN-006). Não define como é cada cupom, a ordem em que
cupons e cartões são aplicados, nem o que acontece com dois pedidos usando o mesmo cupom.

## Decisão

### Cupom

Uma única tabela `cupom`, com a categoria `PROMOCIONAL` ou `TROCA`. Campos comuns: código, validade (data, opcional)
e situação (disponível/usado). (Alterado pelo [DEC-15.2](DEC-15.2-uso-do-cupom-promocional.md): promocional em `cupom_promocional`, sem situação, com o uso
registrado por cliente em `uso_cupom_promocional`; `cupom` fica só com os de troca.)

**Promocional** (criado pelo seed):

| Tipo | Valor | Abate |
|---|---|---|
| `VALOR_FIXO` | reais | dos itens |
| `PERCENTUAL` | % | dos itens, arredondado a 2 casas com `HALF_UP` |
| `FRETE_GRATIS` | — | todo o frete |
| `FRETE_VALOR_FIXO` | reais | do frete |

- O desconto é limitado à sua base (itens ou frete); o que sobra se perde, porque é desconto, não crédito.
- Validade obrigatória: vale até o fim do dia informado.
- Valor mínimo de compra opcional, comparado com o subtotal dos itens (sem frete).
- O cliente digita o código. Cada cliente usa cada promocional uma vez; não há limite global.
- Um promocional por compra, de qualquer tipo, inclusive o de frete (RN-VEN-003).

**De troca:**

- Pertence a um cliente, e só ele o usa; o checkout lista os cupons de troca disponíveis do cliente.
- Valor do cupom gerado na troca (RF-VEN-015): preço unitário do item no pedido × quantidade trocada, sem frete.
- O cupom de troco (RN-VEN-006) também é de troca.
- Validade dada por `livraria.cupom.troca.validade` (`Period`, contado da geração). Sem valor, o cupom não vence; é o
  padrão.

Todo cupom é usado **inteiro** numa única compra.

### Distribuição do pagamento

Total da compra = itens + frete. A distribuição, em `BigDecimal`, segue esta ordem:

1. **Promocional:** abate o desconto da sua base.
2. **Cupons de troca:** cobrem o que restou. O que passar desse restante vira **um cupom de troca de troco** para o
   cliente (RN-VEN-006). Só cupom de troca gera troco.
3. **Cartões:** pagam o restante. O cliente informa o valor de cada cartão, e a soma precisa fechar o restante. Um
   mesmo cartão não aparece duas vezes.
   - Cada cartão paga no mínimo R$ 10,00 (RN-VEN-004).
   - Exceção (RN-VEN-005): havendo cupom e restante menor que R$ 10,00, um único cartão paga esse restante.
   - Restante zero não aceita cartão.

**Cupom desnecessário (RN-VEN-006):** um cupom, de qualquer categoria, é desnecessário se, sem ele, os demais cupons
ainda cobrem o total. Havendo algum, a compra é recusada (422) indicando quais; o `web` avisa já na seleção, e o
cliente escolhe o que retirar. A regra vale também para o promocional, que senão viraria troco ao ser usado junto de
cupons de troca que já cobrem tudo.

Exemplo do DRS: compra de R$ 50,00 com cupons de R$ 20, R$ 40 e R$ 35 é recusada (qualquer um sai e os outros
cobrem). Com R$ 20 e R$ 40, nenhum é desnecessário: a compra é aceita e gera troco de R$ 10,00.

### Uso, reprovação e concorrência

- Os cupons são marcados como **usados ao finalizar** a compra (pedido `EM PROCESSAMENTO`), com bloqueio otimista
  (`@Version`). De dois pedidos simultâneos com o mesmo cupom, o segundo recebe 409
  ([DEC-07](DEC-07-convencoes-da-api-rest.md)). (Alterado pelo [DEC-15.2](DEC-15.2-uso-do-cupom-promocional.md): o promocional grava um uso por cliente, com
  `UNIQUE (cupom_promocional_id, cliente_id)`, apagado na reprovação.)
- A finalização valida a validade e a disponibilidade dos cupons (RN-VEN-007). Pedido `REPROVADA` devolve os cupons
  (voltam a disponíveis).
- O cupom de troco só é gerado quando o pedido é `APROVADA`.

### Cartão no pedido

- Salvo ou novo, o pedido guarda de cada cartão só a cópia definida no
  [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md): bandeira, 4 últimos dígitos e valor.
- Número completo e CVV ficam só em memória. Por isso a operadora fake ([DEC-05.2](DEC-05.2-operadora-de-cartao.md))
  é chamada **dentro da própria requisição de finalização**, que leva o pedido de `EM PROCESSAMENTO` a `APROVADA` ou
  `REPROVADA`.

## Alternativas consideradas

- Cupom de frete como categoria à parte, combinável com outro promocional: mais regras de combinação.
- Desconto percentual no frete: o valor fixo e o frete grátis bastam.
- Tabelas separadas para promocional e troca: as regras de uso consultariam as duas.
- Uso parcial com saldo: dispensaria o troco, mas desvia do RN-VEN-006.
- Excedente do promocional virar troco: transformaria desconto em crédito.
- Descartar automaticamente os cupons desnecessários: o server escolheria por critério que o cliente não vê.
- Divisão igual entre os cartões: o DRS deixa o cliente escolher a forma de pagamento.
- Consumir os cupons só na aprovação: outro pedido poderia usá-los no intervalo.
- Guardar número e CVV até uma validação assíncrona: dado sensível persistido sem necessidade.

## Consequências

- `UmCupomPromocionalPorCompra` (RN-VEN-003), `ValorMinimoPorCartao` (RN-VEN-004, RN-VEN-005) e
  `CuponsDesnecessarios` (RN-VEN-006) são regras puras; `DistribuirPagamento` calcula descontos, troco e restante.
- O seed tem promocionais dos quatro tipos, um vencido e um com valor mínimo, e cupons de troca para os clientes de
  teste.
- O desconto do promocional não é rateado nos itens: o cupom gerado numa troca vale o preço unitário cheio.
