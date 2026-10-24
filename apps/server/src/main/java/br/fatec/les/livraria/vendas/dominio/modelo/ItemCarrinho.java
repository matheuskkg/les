package br.fatec.les.livraria.vendas.dominio.modelo;

import java.util.Objects;

/**
 * Linha do carrinho com um livro e a quantidade desejada (RF-VEN-001, RF-VEN-002). Cada livro aparece uma
 * única vez no carrinho (DEC-16); reincidir na adição soma à linha existente.
 *
 * <p>O preço não fica aqui: o carrinho mostra o preço de venda atual do catálogo, e o pedido é que copia o
 * preço na finalização (DEC-12, DEC-16).</p>
 */
public class ItemCarrinho {

    private final Long id;
    private final Long livroId;
    private int quantidade;

    public ItemCarrinho(Long id, Long livroId, int quantidade) {
        this.id = id;
        this.livroId = Objects.requireNonNull(livroId, "livroId");
        this.quantidade = exigirQuantidade(quantidade);
    }

    /** RF-VEN-002: a quantidade de cada item pode ser alterada, mas nunca fica abaixo de 1. */
    public void alterarQuantidade(int quantidade) {
        this.quantidade = exigirQuantidade(quantidade);
    }

    /** Soma as unidades de uma nova adição do mesmo livro (RF-VEN-001). */
    void somar(int quantidade) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade adicionada deve ser no mínimo 1.");
        }
        this.quantidade = Math.addExact(this.quantidade, quantidade);
    }

    private static int exigirQuantidade(int quantidade) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade do item deve ser no mínimo 1.");
        }
        return quantidade;
    }

    public Long getId() {
        return id;
    }

    public Long getLivroId() {
        return livroId;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
