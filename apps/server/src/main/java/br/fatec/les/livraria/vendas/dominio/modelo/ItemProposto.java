package br.fatec.les.livraria.vendas.dominio.modelo;

/**
 * Item proposto para o carrinho, como a operação o recebe antes de consultar o catálogo e o estoque (RF-VEN-001,
 * RF-VEN-002, RN-VEN-001). É o alvo das regras puras de quantidade e de livro vendável.
 *
 * @param livroId livro desejado; nulo é uma entrada inválida
 * @param quantidade unidades desejadas, no mínimo 1
 */
public record ItemProposto(Long livroId, int quantidade) {

    public static final int QUANTIDADE_MINIMA = 1;
}
