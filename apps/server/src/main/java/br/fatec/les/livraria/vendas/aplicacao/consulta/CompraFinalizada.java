package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

/**
 * Resultado de "Finalizar compra" (RF-VEN-008, RN-VEN-008, DEC-13): o pedido já no status final e, na reprovação, os
 * cartões recusados.
 */
public record CompraFinalizada(Pedido pedido, List<CartaoRecusado> recusas) {

    public CompraFinalizada {
        Objects.requireNonNull(pedido, "pedido");
        recusas = List.copyOf(Objects.requireNonNull(recusas, "recusas"));
    }
}
