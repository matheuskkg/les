package br.fatec.les.livraria.vendas.dominio.troca;

/** Se as trocas não recusadas cobrem toda a quantidade do pedido ({@code TOTAL}) ou só parte dela (DEC-12). */
public enum AbrangenciaDaTroca {
    PARCIAL,
    TOTAL
}
