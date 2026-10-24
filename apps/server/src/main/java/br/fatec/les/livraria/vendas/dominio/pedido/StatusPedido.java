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
    ENTREGUE
}
