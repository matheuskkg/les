package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CartaoNoPagamento;

import org.jspecify.annotations.Nullable;

/** Cartão conferido no pagamento, com o número mascarado (DEC-10), e o valor que ele paga (RN-VEN-004). */
public record CartaoConferidoDto(@Nullable Long cartaoId, String bandeira, String numero, Dinheiro valor) {

    public static CartaoConferidoDto de(CartaoNoPagamento cartao) {
        return new CartaoConferidoDto(
                cartao.cartao().id(),
                cartao.cartao().bandeira().nome(),
                cartao.cartao().numeroMascarado(),
                cartao.valor());
    }
}
