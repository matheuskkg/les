package br.fatec.les.livraria.compartilhado.infraestrutura.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param habilitado executa os seeds na inicialização
 * @param local padrões de recursos dos scripts, separados por vírgula e executados em ordem alfabética do nome do
 *              arquivo
 */
@ConfigurationProperties("livraria.seed")
public record PropriedadesSeed(
        @DefaultValue("true") boolean habilitado,
        @DefaultValue("classpath:seed/*.sql") String local) {
}
