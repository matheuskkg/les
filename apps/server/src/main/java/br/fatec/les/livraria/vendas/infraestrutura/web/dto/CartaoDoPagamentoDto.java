package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeCartao;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.CartaoInformado;

import io.swagger.v3.oas.annotations.media.Schema;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Cartão do pagamento (RF-VEN-006, RN-VEN-004, DEC-10, DEC-11): {@code cartaoId} de um cartão cadastrado ou {@code
 * novo}, nunca os dois, e o valor que ele paga, em texto para não passar por ponto flutuante.
 */
public record CartaoDoPagamentoDto(
        @Nullable Long cartaoId,
        @Nullable CartaoInformadoDto novo,
        @Schema(type = "string", example = "49.88") BigDecimal valor) {

    public CartaoInformado paraCartaoInformado() {
        return new CartaoInformado(new EscolhaDeCartao(cartaoId, novo == null ? null : novo.paraNovoCartao()), valor);
    }
}
