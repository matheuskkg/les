package br.fatec.les.livraria.vendas.aplicacao;

import java.time.Period;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parâmetros da troca (RN-VEN-013, DEC-12).
 *
 * @param prazo dias depois da entrega para pedir a troca, contados até o fim do último dia; por padrão 7 dias
 */
@ConfigurationProperties("livraria.troca")
public record PropriedadesDaTroca(@DefaultValue("P7D") Period prazo) {

    public PropriedadesDaTroca {
        if (prazo == null || prazo.isZero() || prazo.isNegative()) {
            throw new IllegalArgumentException("livraria.troca.prazo deve ser positivo.");
        }
    }
}
