package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.vendas.aplicacao.consulta.ContagemPorStatus;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import java.util.Map;

/** Quantidade de pedidos e de trocas por status, com todos os status presentes (RF-VEN-009, RF-VEN-013). */
public record ContagemPorStatusDto(Map<StatusPedido, Long> pedidos, Map<StatusTroca, Long> trocas) {

    public static ContagemPorStatusDto de(ContagemPorStatus contagem) {
        return new ContagemPorStatusDto(contagem.pedidos(), contagem.trocas());
    }
}
