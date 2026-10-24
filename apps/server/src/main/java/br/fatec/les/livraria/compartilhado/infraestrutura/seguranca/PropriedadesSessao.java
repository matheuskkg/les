package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuração do cookie de sessão (DEC-08).
 *
 * @param cookieSeguro liga o atributo {@code Secure}; desligado no desenvolvimento, que usa HTTP
 */
@ConfigurationProperties("livraria.sessao")
public record PropriedadesSessao(@DefaultValue("false") boolean cookieSeguro) {
}
