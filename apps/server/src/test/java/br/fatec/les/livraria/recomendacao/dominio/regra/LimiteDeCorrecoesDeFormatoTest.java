package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LimiteDeCorrecoesDeFormatoTest {

    private final LimiteDeCorrecoesDeFormato limite = new LimiteDeCorrecoesDeFormato();

    @Test
    void rnfRec001_permiteUmaUnicaCorrecaoDeFormato() {
        assertThat(limite.permite(0)).isTrue();
        assertThat(limite.permite(1)).isFalse();
    }
}
