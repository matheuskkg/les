package br.fatec.les.livraria.vendas.dominio.pedido;

/**
 * Status do ciclo da venda (RF-VEN-008, RN-VEN-008, DEC-12). A API os devolve pelo nome, e a UI troca o {@code _} por
 * espaço, como no DRS ({@code EM PROCESSAMENTO}). Os status de troca ficam na troca.
 */
public enum StatusPedido {

    EM_PROCESSAMENTO,
    APROVADA,
    REPROVADA,
    EM_TRANSPORTE,
    ENTREGUE;

    /**
     * Se o pedido conta como venda na análise e no ranking: {@code APROVADA}, {@code EM TRANSPORTE} e
     * {@code ENTREGUE} (RN-ANA-004, DEC-04, DEC-12).
     */
    public boolean contaComoVenda() {
        return this == APROVADA || this == EM_TRANSPORTE || this == ENTREGUE;
    }
}
