package br.fatec.les.livraria.vendas.dominio.modelo;

/** Situação da sessão de compra (DEC-12). Só um checkout {@link #ABERTO} por cliente. */
public enum SituacaoCheckout {

    /** Segura as reservas enquanto o cliente paga. */
    ABERTO,
    /** O cliente voltou ao carrinho ou inativou a conta. */
    ENCERRADO,
    /** O prazo venceu e os itens foram para os removidos do carrinho (RNF-VEN-001). */
    EXPIRADO,
    /** O pedido foi aprovado e as reservas viraram baixa de estoque. */
    CONCLUIDO
}
