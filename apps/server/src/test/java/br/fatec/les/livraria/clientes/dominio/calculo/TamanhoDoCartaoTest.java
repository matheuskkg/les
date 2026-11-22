package br.fatec.les.livraria.clientes.dominio.calculo;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.AMERICAN_EXPRESS;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.HIPERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.MASTERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.VISA;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** RN-CLI-004 (DEC-18): comprimento do número conforme os tamanhos da bandeira identificada. */
class TamanhoDoCartaoTest {

    private final TamanhoDoCartao calculo = new TamanhoDoCartao();

    @Test
    void rnCli004_aceitaOsTamanhosDaBandeira() {
        assertThat(calculo.aceita("4111111111111111", VISA)).isTrue();
        assertThat(calculo.aceita("5555555555554444", MASTERCARD)).isTrue();
        assertThat(calculo.aceita("378282246310005", AMERICAN_EXPRESS)).isTrue();
        assertThat(calculo.aceita("6062825624080000", HIPERCARD)).isTrue();
    }

    @Test
    void rnCli004_recusaTamanhoForaDaBandeira() {
        assertThat(calculo.aceita("411111111111111", VISA)).isFalse();
        assertThat(calculo.aceita("555555555555444", MASTERCARD)).isFalse();
        assertThat(calculo.aceita("3782822463100051", AMERICAN_EXPRESS)).isFalse();
    }

    @Test
    void rnCli004_aceitaNumeroComMascara() {
        assertThat(calculo.aceita("4111 1111 1111 1111", VISA)).isTrue();
    }

    @Test
    void rnCli004_numeroOuBandeiraAusenteEhRecusado() {
        assertThat(calculo.aceita(null, VISA)).isFalse();
        assertThat(calculo.aceita("4111111111111111", null)).isFalse();
    }
}
