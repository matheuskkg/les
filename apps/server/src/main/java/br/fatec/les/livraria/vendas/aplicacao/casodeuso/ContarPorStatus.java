package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.vendas.aplicacao.consulta.ContagemPorStatus;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocasConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * Conta pedidos e trocas por status para as abas da consulta do admin (RF-VEN-009, RF-VEN-013), completando com zero os
 * status sem registro.
 */
@Component
@RequiredArgsConstructor
public class ContarPorStatus {

    private final PedidosConsultas pedidos;
    private final TrocasConsultas trocas;

    @Transactional(readOnly = true)
    public ContagemPorStatus executar() {
        return new ContagemPorStatus(
                comZeros(StatusPedido.class, pedidos.contarPorStatus()),
                comZeros(StatusTroca.class, trocas.contarPorStatus()));
    }

    private static <S extends Enum<S>> Map<S, Long> comZeros(Class<S> tipo, Map<S, Long> contagem) {
        Map<S, Long> completa = new EnumMap<>(tipo);
        for (S status : tipo.getEnumConstants()) {
            completa.put(status, contagem.getOrDefault(status, 0L));
        }
        return completa;
    }
}
