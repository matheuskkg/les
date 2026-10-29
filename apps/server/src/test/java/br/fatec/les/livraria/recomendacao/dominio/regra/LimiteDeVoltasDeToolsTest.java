package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LimiteDeVoltasDeToolsTest {

    private final LimiteDeVoltasDeTools limite = new LimiteDeVoltasDeTools();

    @Test
    void rnfRec001_permiteAteTresVoltasQueExecutamTools() {
        assertThat(limite.permite(0)).isTrue();
        assertThat(limite.permite(1)).isTrue();
        assertThat(limite.permite(2)).isTrue();
        assertThat(limite.permite(3)).isFalse();
    }
}
