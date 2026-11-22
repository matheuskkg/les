package br.fatec.les.livraria.vendas.aplicacao.consulta;

import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import java.util.Map;

/**
 * Quantidade de pedidos e de trocas por status (RF-VEN-009, RF-VEN-013). Todo status aparece, com zero quando não há
 * nenhum.
 */
public record ContagemPorStatus(Map<StatusPedido, Long> pedidos, Map<StatusTroca, Long> trocas) {

    public ContagemPorStatus {
        pedidos = Map.copyOf(pedidos);
        trocas = Map.copyOf(trocas);
    }
}
