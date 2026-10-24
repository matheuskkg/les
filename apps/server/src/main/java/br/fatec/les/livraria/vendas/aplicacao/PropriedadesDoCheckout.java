package br.fatec.les.livraria.vendas.aplicacao;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parâmetros da sessão de compra (DEC-12, RN-VEN-014). O prazo conta do início do checkout e vai para
 * {@code checkout.expira_em} e para {@code reserva.expira_em}.
 *
 * @param prazo          tempo do checkout, por padrão 60 minutos
 * @param avisoExpiracao antecedência do aviso ao cliente, por padrão 5 minutos
 */
@ConfigurationProperties("livraria.checkout")
public record PropriedadesDoCheckout(
        @DefaultValue("60m") Duration prazo,
        @DefaultValue("5m") Duration avisoExpiracao) {

    public PropriedadesDoCheckout {
        if (prazo == null || prazo.isZero() || prazo.isNegative()) {
            throw new IllegalArgumentException("livraria.checkout.prazo deve ser positivo.");
        }
        if (avisoExpiracao == null || avisoExpiracao.isZero() || avisoExpiracao.isNegative()
                || avisoExpiracao.compareTo(prazo) >= 0) {
            throw new IllegalArgumentException(
                    "livraria.checkout.aviso-expiracao deve ser positivo e menor que o prazo do checkout.");
        }
    }
}
