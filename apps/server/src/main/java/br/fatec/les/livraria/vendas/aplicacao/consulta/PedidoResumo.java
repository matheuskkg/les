package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDateTime;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/**
 * Linha da consulta de pedidos do admin (RF-VEN-009, DEC-12): só as colunas do pedido e a cópia do cliente, sem
 * itens, cartões nem cupons.
 *
 * @param id número do pedido
 * @param status status do pedido
 * @param realizadoEm data e hora da finalização, no fuso da loja
 * @param cliente cópia do cliente gravada no pedido
 * @param total total do pedido
 */
public record PedidoResumo(Long id, StatusPedido status, LocalDateTime realizadoEm, ClienteNoPedido cliente,
        Dinheiro total) {
}
