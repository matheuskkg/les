package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.vendas.aplicacao.consulta.CartaoRecusado;

/** Cartão recusado pela operadora (RN-VEN-007, DEC-05.2). */
public record CartaoRecusadoDto(String ultimosDigitos, String motivo) {

    public static CartaoRecusadoDto de(CartaoRecusado recusa) {
        return new CartaoRecusadoDto(recusa.ultimosDigitos(), recusa.motivo());
    }
}
