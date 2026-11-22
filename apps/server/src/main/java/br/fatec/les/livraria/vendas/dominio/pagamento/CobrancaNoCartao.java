package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Cobrança enviada à operadora para um cartão do pedido (RN-VEN-007, DEC-05.2, DEC-11). Número completo e código de
 * segurança só existem em memória durante a finalização: nunca são gravados nem aparecem no {@link #toString()}.
 */
public record CobrancaNoCartao(String numero, YearMonth validade, String codigoSeguranca, Dinheiro valor) {

    public CobrancaNoCartao {
        Objects.requireNonNull(numero, "numero");
        Objects.requireNonNull(validade, "validade");
        Objects.requireNonNull(codigoSeguranca, "codigoSeguranca");
        Objects.requireNonNull(valor, "valor");
    }

    public String ultimosDigitos() {
        return numero.substring(Math.max(0, numero.length() - 4));
    }

    @Override
    public String toString() {
        return "CobrancaNoCartao[final " + ultimosDigitos() + ", " + valor + "]";
    }
}
