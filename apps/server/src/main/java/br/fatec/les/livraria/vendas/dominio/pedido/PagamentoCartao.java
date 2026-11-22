package br.fatec.les.livraria.vendas.dominio.pedido;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.util.Objects;

/** Quanto cada cartão pagou do pedido (RF-VEN-006, DEC-10, DEC-11). */
public record PagamentoCartao(CartaoNoPedido cartao, Dinheiro valor) {

    public PagamentoCartao {
        Objects.requireNonNull(cartao, "cartao");
        Objects.requireNonNull(valor, "valor");
    }
}
