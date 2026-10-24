package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoResumo;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/**
 * Linha da consulta de pedidos do admin (RF-VEN-009, DEC-12). O nome e o CPF são os copiados no pedido;
 * {@code realizadoEm} em UTC.
 */
public record PedidoListadoDto(Long id, StatusPedido status, Instant realizadoEm, Long clienteId,
        String clienteNome, String clienteCpf, Dinheiro total) {

    public static PedidoListadoDto de(PedidoResumo pedido) {
        return new PedidoListadoDto(pedido.id(), pedido.status(),
                pedido.realizadoEm().atZone(RelogioDoSistema.FUSO).toInstant(), pedido.cliente().id(),
                pedido.cliente().nome(), pedido.cliente().cpf(), pedido.total());
    }
}
