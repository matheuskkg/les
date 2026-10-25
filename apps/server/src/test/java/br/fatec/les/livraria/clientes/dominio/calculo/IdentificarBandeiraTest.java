package br.fatec.les.livraria.clientes.dominio.calculo;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.AMERICAN_EXPRESS;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.ELO;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.HIPERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.MASTERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.VISA;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.bandeiras;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** RN-CLI-005 (DEC-18): bandeira identificada pelo prefixo mais longo do número. */
class IdentificarBandeiraTest {

    private final IdentificarBandeira calculo = new IdentificarBandeira();

    @Test
    void rnCli005_identificaAsBandeirasDoSeed() {
        assertThat(calculo.identificar("4111111111111111", bandeiras())).contains(VISA);
        assertThat(calculo.identificar("5555555555554444", bandeiras())).contains(MASTERCARD);
        assertThat(calculo.identificar("6362970000457013", bandeiras())).contains(ELO);
        assertThat(calculo.identificar("378282246310005", bandeiras())).contains(AMERICAN_EXPRESS);
        assertThat(calculo.identificar("6062825624080000", bandeiras())).contains(HIPERCARD);
    }

    @Test
    void rnCli005_prefixoMaisLongoVenceOPrefixoCurtoDaVisa() {
        assertThat(calculo.identificar("4389350000000000", bandeiras())).contains(ELO);
    }

    @Test
    void rnCli005_faixasDaMastercardSaoInclusivas() {
        assertThat(calculo.identificar("5100000000000000", bandeiras())).contains(MASTERCARD);
        assertThat(calculo.identificar("2221000000000000", bandeiras())).contains(MASTERCARD);
        assertThat(calculo.identificar("2720999999999999", bandeiras())).contains(MASTERCARD);
        assertThat(calculo.identificar("2721000000000000", bandeiras())).isEmpty();
    }

    @Test
    void rnCli005_numeroComMascaraEhIdentificado() {
        assertThat(calculo.identificar("4111 1111 1111 1111", bandeiras())).contains(VISA);
    }

    @Test
    void rnCli005_numeroSemPrefixoConhecidoNaoTemBandeira() {
        assertThat(calculo.identificar("9999999999999999", bandeiras())).isEmpty();
        assertThat(calculo.identificar(null, bandeiras())).isEmpty();
        assertThat(calculo.identificar("", bandeiras())).isEmpty();
    }
}
