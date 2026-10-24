package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Item vendido: a cópia do livro, o preço de venda na finalização e a quantidade (RF-VEN-008, DEC-12).
 *
 * @param id nulo antes de o pedido ser gravado
 */
public record ItemPedido(Long id, LivroNoPedido livro, Dinheiro precoUnitario, int quantidade) {

    public ItemPedido {
        Objects.requireNonNull(livro, "livro");
        Objects.requireNonNull(precoUnitario, "precoUnitario");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade do item deve ser no mínimo 1.");
        }
    }

    public Dinheiro subtotal() {
        return precoUnitario.multiplicar(quantidade);
    }
}
