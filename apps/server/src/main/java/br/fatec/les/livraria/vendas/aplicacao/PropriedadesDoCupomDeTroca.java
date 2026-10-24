package br.fatec.les.livraria.vendas.aplicacao;

import java.time.LocalDate;
import java.time.Period;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parâmetros do cupom de troca (DEC-11), inclusive o de troco (RN-VEN-006).
 *
 * @param validade prazo contado da geração; sem valor, o padrão, o cupom não vence
 */
@ConfigurationProperties("livraria.cupom.troca")
public record PropriedadesDoCupomDeTroca(@Nullable Period validade) {

    public PropriedadesDoCupomDeTroca {
        if (validade != null && (validade.isZero() || validade.isNegative())) {
            throw new IllegalArgumentException("livraria.cupom.troca.validade deve ser positiva.");
        }
    }

    /** @return o último dia de validade de um cupom gerado em {@code geradoEm}, ou nulo se o cupom não vence */
    public @Nullable LocalDate validadeDeUmCupomGeradoEm(LocalDate geradoEm) {
        return validade == null ? null : geradoEm.plus(validade);
    }
}
