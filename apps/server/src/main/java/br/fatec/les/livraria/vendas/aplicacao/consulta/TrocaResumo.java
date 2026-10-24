package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDateTime;

import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

/**
 * Linha da consulta de trocas do admin (RF-VEN-013, DEC-12).
 *
 * @param id número da troca
 * @param pedidoId número do pedido trocado
 * @param status status da troca
 * @param solicitadaEm data e hora da solicitação, no fuso da loja
 * @param cliente cópia do cliente gravada no pedido
 * @param unidades total de unidades na troca
 */
public record TrocaResumo(Long id, Long pedidoId, StatusTroca status, LocalDateTime solicitadaEm,
        ClienteNoPedido cliente, long unidades) {
}
