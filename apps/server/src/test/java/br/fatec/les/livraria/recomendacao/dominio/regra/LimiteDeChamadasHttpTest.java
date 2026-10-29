package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LimiteDeChamadasHttpTest {

    private final LimiteDeChamadasHttp limite = new LimiteDeChamadasHttp();

    @Test
    void rnfRec001_respeitaOMaximoDeChamadasHttpDoProvedor() {
        assertThat(limite.permite(0, 5)).isTrue();
        assertThat(limite.permite(4, 5)).isTrue();
        assertThat(limite.permite(5, 5)).isFalse();
        assertThat(limite.permite(1, 1)).isFalse();
    }
}
