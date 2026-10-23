# Gerenciar Vendas Eletrônicas

Prefixo do grupo: `VEN`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

| ID | Nome | Descrição |
|---|---|---|
| RF-VEN-001 | Gerenciar carrinho de compra | Permitir colocar produtos em um repositório temporário para futura compra (carrinho). Deve ser possível adicionar, alterar, excluir e visualizar itens do carrinho. |
| RF-VEN-002 | Definir quantidade de itens no carrinho | Deve ser possível editar a quantidade de cada item ao adicionar um produto no carrinho e na visualização dos itens já adicionados. |
| RF-VEN-003 | Realizar compra | Deve ser possível a partir de um carrinho de compra realizar uma compra. |
| RF-VEN-004 | Calcular frete | O sistema deve calcular o frete da compra com base nos itens selecionados e no endereço apontado pelo cliente. |
| RF-VEN-005 | Selecionar endereço de entrega | O cliente pode selecionar um endereço previamente cadastrado ou cadastrar um novo; um novo endereço pode ser incorporado ao perfil. |
| RF-VEN-006 | Selecionar forma de pagamento | O cliente pode selecionar um cartão previamente cadastrado ou cadastrar um novo (com opção de incorporá-lo ao perfil). Também pode usar cupom de troca ou cupom promocional válido. |
| RF-VEN-007 | Uso de cupom para pagamento | Deve-se possibilitar pagamento com cupons de troca, promocionais e cartão de crédito. |
| RF-VEN-008 | Finalizar compra | A compra é finalizada após a seleção da forma de pagamento e endereço de entrega; o status passa a ser EM PROCESSAMENTO. |
| RF-VEN-009 | Despachar produtos para entrega | Administrador seleciona vendas aprovadas para entrega; status EM TRANSPORTE. |
| RF-VEN-010 | Produtos entregues | Administrador confirma a entrega de uma compra; status ENTREGUE. |
| RF-VEN-011 | Solicitar troca | Cliente pode solicitar a troca de um item a partir da visualização de suas compras. |
| RF-VEN-012 | Autorizar trocas | Administrador autoriza compras com status EM TROCA; status TROCA AUTORIZADA. |
| RF-VEN-013 | Visualização de trocas | Administrador visualiza todos os pedidos de troca ou compras com status EM TROCA. |
| RF-VEN-014 | Confirmar recebimento de itens para troca | Administrador confirma o recebimento de itens em troca e informa se retornam ao estoque; em caso positivo, dá-se entrada no estoque. |
| RF-VEN-015 | Gerar cupom de troca após recebimento | Ao confirmar a chegada dos itens, o sistema gera um cupom de troca disponível ao cliente para compras futuras. |

## Requisitos não funcionais

| ID | Nome | Descrição |
|---|---|---|
| RNF-VEN-001 | Apresentar itens retirados do carrinho | A listagem do carrinho deve apresentar os produtos removidos por atingirem o prazo para finalização da compra (tempo conforme parâmetro do sistema). A opção comprar deve ser desabilitada e os itens adicionados novamente. |

## Regras de negócio

| ID | Nome | Descrição |
|---|---|---|
| RN-VEN-001 | Validar estoque para adição no carrinho | Não permitir adicionar ao carrinho item sem estoque, nem quantidade maior que a disponível. |
| RN-VEN-002 | Validar estoque para compra | Se o estoque mudar entre a adição ao carrinho e a finalização: notificar o usuário, atualizar a quantidade no carrinho e remover itens indisponíveis com alerta. |
| RN-VEN-003 | Uso de cupom promocional | Apenas um cupom promocional pode ser utilizado por compra. |
| RN-VEN-004 | Uso de diversos cartões | Uma compra pode ser paga com mais de um cartão; valor mínimo de R$ 10,00 por cartão. |
| RN-VEN-005 | Uso de cupons junto a cartão | Com cupons e cartões juntos, considera-se o valor máximo dos cupons; só nesse caso o cartão pode pagar menos de R$ 10,00 (ex.: compra de R$ 35,00 = R$ 30,00 em cupons + R$ 5,00 no cartão). |
| RN-VEN-006 | Gerar cupom de troca | Gera-se cupom de troca quando os cupons usados superam o valor da compra. Não permitir cupons desnecessários (ex.: compra de R$ 50,00 com cupons de R$ 20, R$ 40 e R$ 35: aceitar só dois e gerar cupom da diferença). |
| RN-VEN-007 | Validar forma de pagamento na finalização | Após a finalização, validar validade e veracidade dos cupons e o aceite da operadora de cartão. |
| RN-VEN-008 | Status conforme aprovação do pagamento | Pagamento validado: status APROVADA; caso contrário, REPROVADA. |
| RN-VEN-009 | Status para transporte | Compra selecionada para entrega pelo administrador passa a EM TRANSPORTE. |
| RN-VEN-010 | Status após entrega | Compra marcada como entregue pelo administrador passa a ENTREGUE. |
| RN-VEN-011 | Gerar pedido de troca | Todo item selecionado para troca gera um pedido de troca com status EM TROCA; troca da compra inteira também fica EM TROCA. |
| RN-VEN-012 | Status após recebimento de troca | Confirmado o recebimento dos itens em troca, o pedido/compra passa a TROCADO. |
| RN-VEN-013 | Validação para solicitar troca | Somente itens de pedidos com status ENTREGUE podem receber solicitação de troca. |
| RN-VEN-014 | Bloqueio de produtos | Ao adicionar ao carrinho, o item é bloqueado temporariamente. O bloqueio é retirado se a compra não for efetivada/aprovada no prazo parametrizado, contado a partir do último item incluído. O usuário é notificado 5 minutos antes de expirar; expirado, os itens são removidos e desbloqueados. |
| RN-VEN-015 | Retirar item do carrinho | Quando um item for desbloqueado, todos os itens do mesmo produto devem ser retirados do carrinho que gerou o bloqueio. |
| RN-VEN-016 | Notificação de autorização de troca | Ao autorizar uma troca, o sistema gera notificação ao cliente. |

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RF-VEN-001 | ❌ |  |  |
| RF-VEN-002 | ❌ |  |  |
| RF-VEN-003 | ❌ |  |  |
| RF-VEN-004 | ❌ |  | Frete fake definido em [DEC-05.3](../decisoes/DEC-05.3-frete.md): base pela região do CEP + R$ 2,00 por 0,5 kg adicional (maior entre peso real e cúbico). |
| RF-VEN-005 | ❌ |  | O checkout também pede o endereço de cobrança (padrão: igual à entrega); o pedido guarda cópia dos dois ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RF-VEN-006 | ❌ |  | O CVV de cada cartão é pedido a cada compra, inclusive dos salvos, e nunca é gravado ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). Cupom promocional pelo código; de troca entre os do cliente ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RF-VEN-007 | ❌ |  | Ordem: promocional, cupons de troca, cartões ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RF-VEN-008 | ❌ |  |  |
| RF-VEN-009 | ❌ |  |  |
| RF-VEN-010 | ❌ |  |  |
| RF-VEN-011 | ❌ |  |  |
| RF-VEN-012 | ❌ |  |  |
| RF-VEN-013 | ❌ |  |  |
| RF-VEN-014 | ❌ |  |  |
| RF-VEN-015 | ❌ |  | Valor = preço unitário do item no pedido × quantidade, sem frete; validade por `livraria.cupom.troca.validade` (padrão: sem validade) ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RNF-VEN-001 | ❌ |  |  |
| RN-VEN-001 | ❌ |  |  |
| RN-VEN-002 | ❌ |  |  |
| RN-VEN-003 | ❌ |  | Vale para os quatro tipos de promocional, inclusive os de frete (`FRETE_GRATIS`, `FRETE_VALOR_FIXO`) ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). Extensão: o DRS não prevê cupom de frete. |
| RN-VEN-004 | ❌ |  | O cliente informa o valor de cada cartão; a soma fecha o restante após os cupons ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RN-VEN-005 | ❌ |  | Exceção: havendo cupom e restante menor que R$ 10,00, um único cartão paga o restante ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RN-VEN-006 | ❌ |  | Só cupom de troca gera troco; o excedente do promocional se perde. Cupom desnecessário (sem ele os demais cobrem o total) recusa a compra com 422 ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RN-VEN-007 | ❌ |  | Operadora fake definida em [DEC-05.2](../decisoes/DEC-05.2-operadora-de-cartao.md): reprova cartão vencido ([DEC-06](../decisoes/DEC-06-validade-do-cartao.md)), terminado em `0000` ou com CVV `000`. Cupons validados quanto a validade e disponibilidade; operadora chamada na própria requisição de finalização ([DEC-11](../decisoes/DEC-11-cupons-e-distribuicao-do-pagamento.md)). |
| RN-VEN-008 | ❌ |  |  |
| RN-VEN-009 | ❌ |  |  |
| RN-VEN-010 | ❌ |  |  |
| RN-VEN-011 | ❌ |  |  |
| RN-VEN-012 | ❌ |  |  |
| RN-VEN-013 | ❌ |  |  |
| RN-VEN-014 | ❌ |  | Prazo parametrizado em [DEC-05.1](../decisoes/DEC-05.1-bloqueio-do-carrinho.md): `livraria.carrinho.prazo-bloqueio` (padrão 30 min), aviso 5 min antes. |
| RN-VEN-015 | ❌ |  |  |
| RN-VEN-016 | ❌ |  |  |
