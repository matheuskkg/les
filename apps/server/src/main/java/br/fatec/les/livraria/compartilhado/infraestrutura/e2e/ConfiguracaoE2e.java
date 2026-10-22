package br.fatec.les.livraria.compartilhado.infraestrutura.e2e;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/** Apoio aos testes E2E (DEC-09). Apaga dados, então só é aceito junto do banco em memória ({@code h2}). */
@Configuration
@Profile("e2e")
public class ConfiguracaoE2e {

    public ConfiguracaoE2e(Environment ambiente) {
        if (!ambiente.matchesProfiles("h2") || ambiente.matchesProfiles("postgres")) {
            throw new IllegalStateException("O profile e2e só pode ser usado junto do h2 (ex.: h2,e2e).");
        }
    }
}
