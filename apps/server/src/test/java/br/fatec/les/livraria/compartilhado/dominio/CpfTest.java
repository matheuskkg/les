package br.fatec.les.livraria.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", "111.444.777-35"})
    void aceitaCpfValidoComOuSemMascara(String cpf) {
        assertThat(Cpf.ehValido(cpf)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"529.982.247-24", "111.111.111-11", "5299822472", "529982247250", "529.982.247_25"})
    void recusaCpfInvalido(String cpf) {
        assertThat(Cpf.ehValido(cpf)).isFalse();
    }

    @Test
    void guardaSomenteDigitosEFormata() {
        Cpf cpf = new Cpf("529.982.247-25");

        assertThat(cpf.numero()).isEqualTo("52998224725");
        assertThat(cpf.formatado()).isEqualTo("529.982.247-25");
        assertThat(cpf).isEqualTo(new Cpf("52998224725"));
    }

    @Test
    void construtorRecusaCpfInvalido() {
        assertThatThrownBy(() -> new Cpf("123")).isInstanceOf(IllegalArgumentException.class);
    }
}
