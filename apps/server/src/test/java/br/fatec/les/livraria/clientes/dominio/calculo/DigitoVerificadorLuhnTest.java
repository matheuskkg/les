package br.fatec.les.livraria.clientes.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** RN-CLI-004 (DEC-18): dígito verificador do número do cartão pelo algoritmo de Luhn. */
class DigitoVerificadorLuhnTest {

    private final DigitoVerificadorLuhn calculo = new DigitoVerificadorLuhn();

    @Test
    void rnCli004_aceitaOsNumerosValidosDasBandeirasDoSeed() {
        assertThat(calculo.valido("4111111111111111")).isTrue();
        assertThat(calculo.valido("5555555555554444")).isTrue();
        assertThat(calculo.valido("6362970000457013")).isTrue();
        assertThat(calculo.valido("378282246310005")).isTrue();
        assertThat(calculo.valido("6062825624080000")).isTrue();
    }

    @Test
    void rnCli004_recusaNumeroComDigitoVerificadorErrado() {
        assertThat(calculo.valido("4111111111111112")).isFalse();
        assertThat(calculo.valido("5555555555554445")).isFalse();
    }

    @Test
    void rnCli004_aceitaNumeroComMascara() {
        assertThat(calculo.valido("4111 1111 1111 1111")).isTrue();
    }

    @Test
    void rnCli004_numeroVazioNaoTemDigitoVerificador() {
        assertThat(calculo.valido(null)).isFalse();
        assertThat(calculo.valido("")).isFalse();
        assertThat(calculo.valido("****")).isFalse();
    }
}
