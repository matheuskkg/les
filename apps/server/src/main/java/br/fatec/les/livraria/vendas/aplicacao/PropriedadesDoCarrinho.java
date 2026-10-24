package br.fatec.les.livraria.vendas.aplicacao;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parâmetros do carrinho (DEC-05.1, RN-VEN-014). O prazo do bloqueio é contado a partir do último item
 * incluído no carrinho e vai para {@code carrinho.expira_em} e para {@code reserva.expira_em}.
 *
 * <p>O aviso de expiração sai quando faltar {@code avisoExpiracao} ou menos para o vencimento (RN-VEN-014).</p>
 *
 * @param prazoBloqueio  tempo do bloqueio, por padrão 30 minutos
 * @param avisoExpiracao antecedência do aviso ao cliente, por padrão 5 minutos (fixada pelo DRS)
 */
@ConfigurationProperties("livraria.carrinho")
public record PropriedadesDoCarrinho(
        @DefaultValue("30m") Duration prazoBloqueio,
        @DefaultValue("5m") Duration avisoExpiracao) {

    public PropriedadesDoCarrinho {
        if (prazoBloqueio == null || prazoBloqueio.isZero() || prazoBloqueio.isNegative()) {
            throw new IllegalArgumentException("livraria.carrinho.prazo-bloqueio deve ser positivo.");
        }
        if (avisoExpiracao == null || avisoExpiracao.isZero() || avisoExpiracao.isNegative()
                || avisoExpiracao.compareTo(prazoBloqueio) >= 0) {
            throw new IllegalArgumentException(
                    "livraria.carrinho.aviso-expiracao deve ser positivo e menor que o prazo do bloqueio.");
        }
    }
}
