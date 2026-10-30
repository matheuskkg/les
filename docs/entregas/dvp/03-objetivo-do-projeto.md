# 3. Objetivo do Projeto

Desenvolver o *Livraria*, sistema web de e-commerce de livros capaz de:

- manter o cadastro de clientes com autenticação, endereços de entrega e cobrança, cartões de crédito (com cartão
  preferencial), alteração de senha e inativação da conta (RF-CLI-001 a RF-CLI-008), com ranking calculado a
  partir do perfil de compra (RN-CLI-007, DEC-04);
- apresentar a vitrine e a consulta de livros com filtros combinados e busca livre (RF-LIV-005), com preço de
  venda calculado a partir do custo e do grupo de precificação (RF-EST-002);
- gerenciar o carrinho de compras com bloqueio por prazo: ao adicionar um item, o estoque é reservado; não
  finalizada a compra no prazo parametrizado, a reserva expira, os itens são retirados e o cliente é avisado por
  notificação (RF-VEN-001 e RF-VEN-002; RN-VEN-001, RN-VEN-002, RN-VEN-014 e RN-VEN-015; DEC-05.1);
- realizar o checkout com frete calculado por CEP e dimensão/peso (RF-VEN-004, DEC-05.3), seleção de endereço
  (RF-VEN-005) e pagamento combinando um ou
  mais cartões de crédito e cupons promocionais e de troca (RF-VEN-006, RF-VEN-007), sem cobrar abaixo do
  mínimo por cartão nem desperdiçar cupons (RN-VEN-003 a RN-VEN-006, DEC-11);
- concluir a compra com validação pela operadora de cartão e ciclar o status do pedido (RF-VEN-008 a RF-VEN-010,
  RN-VEN-007 a RN-VEN-010, DEC-12): o pedido passa de EM PROCESSAMENTO para APROVADA ou REPROVADA, depois para
  EM TRANSPORTE e ENTREGUE, com baixa de estoque na aprovação
  (RF-EST-003) e notificações ao cliente e ao administrador em cada mudança, com a validação do pagamento
  processada depois de gravado o pedido (DEC-13);
- gerenciar trocas: solicitação pelo cliente na entrega, autorização ou recusa pelo administrador, confirmação do
  recebimento por item (com reentrada no estoque quando aplicável, RF-EST-004) e geração automática do cupom de
  troca (RF-VEN-011 a RF-VEN-015, RN-VEN-011 a RN-VEN-016);
- registrar e auditar transações (RNF-GER-002) e manter o histórico de todas as compras do cliente, inclusive as
  reprovadas (RF-CLI-005);
- analisar as vendas por mês e categoria, com gráfico e exportação em CSV (RF-ANA-001 a RF-ANA-004);
- recomendar livros e oferecer assistente de conversa com IA generativa, personalizados pelo histórico e pelas
  avaliações do cliente (RNF-REC-001, DEC-03).

Com o sistema, o cliente reserva o livro ao colocá-lo no carrinho, não o perde enquanto decide, divide o pagamento
entre cartões e cupons e pede a troca pelo site. O controle do fluxo de vendas deixa de ser feito à mão, o que
reduz os erros manuais no acompanhamento dos pedidos. A análise de vendas e o assistente de IA dão ao administrador
da loja meios de ajustar as sugestões ao perfil do comprador e, com isso, converter mais visitas em compras.
