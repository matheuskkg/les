package br.fatec.les.livraria.vendas.dominio.pagamento;

/** Situação do cupom de troca (DEC-11): usado ao finalizar a compra e devolvido se o pedido for reprovado. */
public enum SituacaoCupom {
    DISPONIVEL,
    USADO
}
