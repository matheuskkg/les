package br.fatec.les.livraria.compartilhado.infraestrutura.e2e;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ConfiguracaoE2eTest {

    private static MockEnvironment comProfiles(String... profiles) {
        MockEnvironment ambiente = new MockEnvironment();
        ambiente.setActiveProfiles(profiles);
        return ambiente;
    }

    @Test
    void aceitaJuntoDoH2() {
        assertThatCode(() -> new ConfiguracaoE2e(comProfiles("h2", "e2e"))).doesNotThrowAnyException();
    }

    @Test
    void recusaSemH2() {
        assertThatThrownBy(() -> new ConfiguracaoE2e(comProfiles("e2e"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recusaComPostgres() {
        assertThatThrownBy(() -> new ConfiguracaoE2e(comProfiles("h2", "postgres", "e2e")))
                .isInstanceOf(IllegalStateException.class);
    }
}
