package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

/**
 * Entrada de {@code POST /api/me/carrinho/itens} (RF-VEN-001, RF-VEN-002, DEC-07).
 *
 * @param livroId livro a adicionar
 * @param quantidade unidades a somar à linha existente, no mínimo 1
 */
public record AdicaoDeItem(Long livroId, int quantidade) {}
