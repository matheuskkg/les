package br.fatec.les.livraria.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void normalizaParaMinusculasSemEspacos() {
        assertThat(new Email("  Maria.Silva@Exemplo.com ").endereco()).isEqualTo("maria.silva@exemplo.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"maria", "maria@", "maria@exemplo", "ma ria@exemplo.com", "a@b@c.com"})
    void recusaFormatoInvalido(String email) {
        assertThat(Email.ehValido(email)).isFalse();
        assertThatThrownBy(() -> new Email(email)).isInstanceOf(IllegalArgumentException.class);
    }
}
