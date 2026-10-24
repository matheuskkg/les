package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDate;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;

/**
 * Pedido com as trocas, para o detalhe (RF-CLI-005, RF-VEN-009, RF-VEN-011, DEC-12).
 *
 * @param trocas   o pedido e as trocas dele
 * @param trocaAte último dia para pedir troca, inteiro; nulo se o pedido não foi entregue (RN-VEN-013)
 */
public record PedidoDetalhado(TrocasDoPedido trocas, @Nullable LocalDate trocaAte) {

    public PedidoDetalhado {
        Objects.requireNonNull(trocas, "trocas");
    }
}
