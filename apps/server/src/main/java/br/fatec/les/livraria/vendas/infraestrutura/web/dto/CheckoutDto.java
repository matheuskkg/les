package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CheckoutConsultado;

import java.time.Instant;
import java.util.List;

/**
 * Checkout aberto na API (RF-VEN-003, DEC-07, DEC-12). {@code iniciadoEm} e {@code expiraEm} em UTC; o prazo é só para
 * exibição (DEC-13).
 */
public record CheckoutDto(
        Long id,
        List<ItemComPrecoDto> itens,
        Dinheiro subtotal,
        int quantidadeDeItens,
        Instant iniciadoEm,
        Instant expiraEm) {

    public static CheckoutDto de(CheckoutConsultado checkout) {
        return new CheckoutDto(
                checkout.id(),
                checkout.itens().stream().map(ItemComPrecoDto::de).toList(),
                checkout.subtotal(),
                checkout.quantidadeDeItens(),
                checkout.iniciadoEm().atZone(RelogioDoSistema.FUSO).toInstant(),
                checkout.expiraEm().atZone(RelogioDoSistema.FUSO).toInstant());
    }
}
