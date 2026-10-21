package br.fatec.les.livraria.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DinheiroTest {

    @Test
    void normalizaParaDuasCasasComHalfEven() {
        assertThat(Dinheiro.de("10.005").valor()).isEqualByComparingTo("10.00");
        assertThat(Dinheiro.de("10.015").valor()).isEqualByComparingTo("10.02");
        assertThat(Dinheiro.de("10").valor().scale()).isEqualTo(2);
    }

    @Test
    void valoresIguaisComEscalasDiferentesSaoIguais() {
        assertThat(Dinheiro.de("10")).isEqualTo(Dinheiro.de("10.00"));
    }

    @Test
    void operacoes() {
        Dinheiro trinta = Dinheiro.de("30.00");
        Dinheiro cinco = Dinheiro.de("5.00");

        assertThat(trinta.somar(cinco)).isEqualTo(Dinheiro.de("35"));
        assertThat(cinco.subtrair(trinta)).isEqualTo(Dinheiro.de("-25"));
        assertThat(Dinheiro.de("19.90").multiplicar(3)).isEqualTo(Dinheiro.de("59.70"));
        assertThat(Dinheiro.de("10.00").multiplicar(new BigDecimal("1.255"))).isEqualTo(Dinheiro.de("12.55"));
    }

    @Test
    void comparacoes() {
        Dinheiro dez = Dinheiro.de("10");
        Dinheiro cinco = Dinheiro.de("5");

        assertThat(dez.maiorQue(cinco)).isTrue();
        assertThat(cinco.menorQue(dez)).isTrue();
        assertThat(Dinheiro.maior(dez, cinco)).isEqualTo(dez);
        assertThat(Dinheiro.menor(dez, cinco)).isEqualTo(cinco);
        assertThat(Dinheiro.ZERO.ehZero()).isTrue();
        assertThat(cinco.subtrair(dez).ehNegativo()).isTrue();
        assertThat(dez.ehPositivo()).isTrue();
    }
}
