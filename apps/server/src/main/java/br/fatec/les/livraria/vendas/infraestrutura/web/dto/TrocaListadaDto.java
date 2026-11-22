package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.vendas.aplicacao.consulta.TrocaResumo;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import java.time.Instant;

/**
 * Linha da consulta de trocas do admin (RF-VEN-013, DEC-12). O nome e o CPF são os copiados no pedido; {@code
 * solicitadaEm} em UTC.
 */
public record TrocaListadaDto(
        Long id,
        Long pedidoId,
        StatusTroca status,
        Instant solicitadaEm,
        Long clienteId,
        String clienteNome,
        String clienteCpf,
        long unidades) {

    public static TrocaListadaDto de(TrocaResumo troca) {
        return new TrocaListadaDto(
                troca.id(),
                troca.pedidoId(),
                troca.status(),
                PedidoDetalheDto.emUtc(troca.solicitadaEm()),
                troca.cliente().id(),
                troca.cliente().nome(),
                troca.cliente().cpf(),
                troca.unidades());
    }
}
