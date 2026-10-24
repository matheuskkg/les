package br.fatec.les.livraria.vendas.aplicacao;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parâmetros do carrinho (DEC-05.1, RN-VEN-014). O prazo do bloqueio é contado a partir do último item
 * incluído no carrinho e vai para {@code carrinho.expira_em} e para {@code reserva.expira_em}.
 *
 * @param prazoBloqueio tempo do bloqueio, por padrão 30 minutos
 */
@ConfigurationProperties("livraria.carrinho")
public record PropriedadesDoCarrinho(@DefaultValue("30m") Duration prazoBloqueio) {

    public PropriedadesDoCarrinho {
        if (prazoBloqueio == null || prazoBloqueio.isZero() || prazoBloqueio.isNegative()) {
            throw new IllegalArgumentException("livraria.carrinho.prazo-bloqueio deve ser positivo.");
        }
    }
}
