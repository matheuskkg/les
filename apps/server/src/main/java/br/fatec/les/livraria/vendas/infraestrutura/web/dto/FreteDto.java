package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FreteCalculado;

/** Frete do checkout e o total com ele, antes de cupons (RF-VEN-004, DEC-05.3). */
public record FreteDto(Dinheiro frete, Dinheiro subtotal, Dinheiro total) {

    public static FreteDto de(FreteCalculado calculado) {
        return new FreteDto(calculado.frete(), calculado.subtotal(), calculado.total());
    }
}
