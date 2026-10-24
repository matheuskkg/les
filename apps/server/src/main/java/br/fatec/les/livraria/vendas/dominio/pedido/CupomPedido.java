package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/** Cupom usado no pedido e o valor que ele abateu (RF-VEN-007, DEC-11, DEC-12). */
public record CupomPedido(CupomNoPedido cupom, Dinheiro valorAbatido) {

    public CupomPedido {
        Objects.requireNonNull(cupom, "cupom");
        Objects.requireNonNull(valorAbatido, "valorAbatido");
    }
}
