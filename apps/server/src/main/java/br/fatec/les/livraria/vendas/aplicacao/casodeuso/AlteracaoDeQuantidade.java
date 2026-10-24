package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

/**
 * Entrada de {@code PUT /api/me/carrinho/itens/{livroId}} (RF-VEN-002, DEC-07). Define a quantidade da
 * linha; zero não é aceito, e a remoção tem operação própria.
 *
 * @param quantidade nova quantidade, no mínimo 1
 */
public record AlteracaoDeQuantidade(int quantidade) {
}
