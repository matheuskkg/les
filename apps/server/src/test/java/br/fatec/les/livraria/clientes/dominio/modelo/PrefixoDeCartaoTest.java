package br.fatec.les.livraria.clientes.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** RN-CLI-005 (DEC-18): prefixo puro ou faixa inclusiva da tabela de bandeiras. */
class PrefixoDeCartaoTest {

    @Test
    void rnCli005_prefixoPuroCasaComOInicioDoNumero() {
        PrefixoDeCartao visa = PrefixoDeCartao.de("4");

        assertThat(visa.casaCom("4111111111111111")).isTrue();
        assertThat(visa.casaCom("5111111111111111")).isFalse();
        assertThat(visa.tamanho()).isEqualTo(1);
        assertThat(visa.texto()).isEqualTo("4");
    }

    @Test
    void rnCli005_faixaInclusivaAceitaOsExtremosEOInterior() {
        PrefixoDeCartao mastercard = PrefixoDeCartao.de("2221-2720");

        assertThat(mastercard.casaCom("2221000000000000")).isTrue();
        assertThat(mastercard.casaCom("2500000000000000")).isTrue();
        assertThat(mastercard.casaCom("2720999999999999")).isTrue();
        assertThat(mastercard.casaCom("2721000000000000")).isFalse();
        assertThat(mastercard.casaCom("2220000000000000")).isFalse();
        assertThat(mastercard.tamanho()).isEqualTo(4);
        assertThat(mastercard.texto()).isEqualTo("2221-2720");
    }

    @Test
    void rnCli005_numeroCurtoNaoCasaComOPrefixo() {
        assertThat(PrefixoDeCartao.de("438935").casaCom("4389")).isFalse();
    }

    @Test
    void rnCli005_prefixoInvalidoEhRejeitado() {
        assertThatThrownBy(() -> PrefixoDeCartao.de("abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PrefixoDeCartao.de("51-5")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PrefixoDeCartao.de("55-51")).isInstanceOf(IllegalArgumentException.class);
    }
}
