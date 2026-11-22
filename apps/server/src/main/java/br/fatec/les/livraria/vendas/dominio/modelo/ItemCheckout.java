package br.fatec.les.livraria.vendas.dominio.modelo;

import java.util.Objects;

/**
 * Livro e quantidade levados do carrinho ao checkout (DEC-12). A linha é única por livro (DEC-16); para mudar a
 * quantidade, o cliente volta ao carrinho.
 *
 * @param livroId livro
 * @param quantidade unidades, no mínimo 1
 */
public record ItemCheckout(Long livroId, int quantidade) {

    public ItemCheckout {
        Objects.requireNonNull(livroId, "livroId");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade do item deve ser no mínimo 1.");
        }
    }
}
