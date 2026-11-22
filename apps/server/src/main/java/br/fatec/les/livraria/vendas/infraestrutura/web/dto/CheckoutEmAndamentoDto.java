package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CheckoutEmAndamento;

import java.time.Instant;

/**
 * Sessão de compra aberta do cliente, na API (RF-VEN-003, DEC-07, DEC-12). {@code expiraEm} em UTC e só para exibição
 * (DEC-13).
 */
public record CheckoutEmAndamentoDto(Instant expiraEm, int quantidadeDeItens) {

    public static CheckoutEmAndamentoDto de(CheckoutEmAndamento checkout) {
        return new CheckoutEmAndamentoDto(
                checkout.expiraEm().atZone(RelogioDoSistema.FUSO).toInstant(), checkout.quantidadeDeItens());
    }
}
