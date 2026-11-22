package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Cartão da finalização (RF-VEN-006, DEC-10, DEC-11): como no {@link CartaoDoPagamentoDto}, mais o código de segurança,
 * que segue só para a operadora e não é gravado.
 */
public record CartaoDaFinalizacaoDto(
        @Nullable Long cartaoId,
        @Nullable CartaoInformadoDto novo,
        @Schema(type = "string", example = "49.88") BigDecimal valor,
        @Schema(example = "123") String codigoSeguranca) {

    public CartaoDoPagamentoDto paraCartaoDoPagamento() {
        return new CartaoDoPagamentoDto(cartaoId, novo, valor);
    }

    @Override
    public String toString() {
        return "CartaoDaFinalizacaoDto[cartaoId=" + cartaoId + ", valor=" + valor + "]";
    }
}
