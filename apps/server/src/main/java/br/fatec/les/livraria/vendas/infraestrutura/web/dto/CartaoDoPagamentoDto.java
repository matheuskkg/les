package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeCartao;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.CartaoInformado;

/**
 * Cartão do pagamento (RF-VEN-006, RN-VEN-004, DEC-10, DEC-11): {@code cartaoId} de um cartão cadastrado ou
 * {@code novo}, nunca os dois, e o valor que ele paga.
 */
public record CartaoDoPagamentoDto(@Nullable Long cartaoId, @Nullable CartaoInformadoDto novo, BigDecimal valor) {

    public CartaoInformado paraCartaoInformado() {
        return new CartaoInformado(new EscolhaDeCartao(cartaoId, novo == null ? null : novo.paraNovoCartao()), valor);
    }
}
