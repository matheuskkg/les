package br.fatec.les.livraria.vendas.dominio.pagamento;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Valor que o cliente quer pagar num cartão (RN-VEN-004, DEC-11). O valor fica como informado, sem arredondar, para
 * que as regras recusem centavos fracionados em vez de corrigi-los.
 *
 * @param numero número do cartão, só dígitos; identifica o cartão, salvo ou novo
 */
public record PagamentoEmCartao(String numero, BigDecimal valor) {

    public PagamentoEmCartao {
        Objects.requireNonNull(numero, "numero");
    }
}
