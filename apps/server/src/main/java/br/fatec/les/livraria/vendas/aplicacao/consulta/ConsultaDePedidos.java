package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/**
 * Filtros da consulta de pedidos do admin como chegam da API (RF-VEN-009, DEC-07, DEC-12). Todos são opcionais e se
 * combinam com E; status repetidos combinam com OU.
 *
 * @param numero número do pedido
 * @param status status desejados; vazio não filtra
 * @param cliente trecho do nome do cliente
 * @param cpf CPF do cliente, com ou sem máscara
 * @param realizadoEmDe dia inicial da finalização, inclusive
 * @param realizadoEmAte dia final da finalização, inclusive
 */
public record ConsultaDePedidos(
        Long numero,
        List<StatusPedido> status,
        String cliente,
        String cpf,
        LocalDate realizadoEmDe,
        LocalDate realizadoEmAte) {

    public ConsultaDePedidos {
        status = status == null ? List.of() : status.stream().filter(Objects::nonNull).distinct().toList();
    }
}
