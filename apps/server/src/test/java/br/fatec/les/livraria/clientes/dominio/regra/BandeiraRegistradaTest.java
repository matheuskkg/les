package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.MASTERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.bandeiras;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comCartoes;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.calculo.IdentificarBandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** RN-CLI-005 (DEC-18): bandeira do cartão identificada pelo número. */
class BandeiraRegistradaTest {

    private final BandeiraRegistrada regra = new BandeiraRegistrada(new IdentificarBandeira());

    @Test
    void rnCli005_numeroVisaIdentificaABandeiraSemViolacao() {
        Violacoes violacoes = new Violacoes();

        List<Optional<Bandeira>> identificadas = regra.identificar(padrao(), violacoes, bandeiras());

        assertThat(violacoes.lista()).isEmpty();
        assertThat(identificadas).singleElement()
                .satisfies(bandeira -> assertThat(bandeira).get().extracting(Bandeira::nome).isEqualTo("Visa"));
    }

    @Test
    void rnCli005_numeroComMascaraTambemIdentificaABandeira() {
        NovoCartao mascarado = new NovoCartao("4111 1111 1111 1111", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(comCartoes(List.of(mascarado)))).isEmpty();
    }

    @Test
    void rnCli005_numeroSemBandeiraNoCadastroViolaARegraComOIndiceDoCartao() {
        NovoCartao desconhecido = new NovoCartao("9999999999999999", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(comCartoes(List.of(cartao(), desconhecido))))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-005");
                    assertThat(violacao.mensagem()).isEqualTo("Cartão 2: bandeira do cartão não aceita.");
                });
    }

    @Test
    void rnCli005_numeroSemBandeiraEmUmCartaoViolaARegraSemIndice() {
        NovoCartao desconhecido = new NovoCartao("9999999999999999", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(desconhecido))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-005");
                    assertThat(violacao.mensagem()).isEqualTo("Bandeira do cartão não aceita.");
                });
    }

    @Test
    void rnCli005_bandeirasIdentificadasFicamAlinhadasAosCartoes() {
        NovoCartao mastercard = new NovoCartao("5555555555554444", "ANA SOUZA", cartao().validade());
        NovoCartao desconhecido = new NovoCartao("9999999999999999", "ANA SOUZA", cartao().validade());
        Violacoes violacoes = new Violacoes();

        List<Optional<Bandeira>> identificadas = regra.identificar(
                comCartoes(List.of(cartao(), desconhecido, mastercard)), violacoes, bandeiras());

        assertThat(identificadas).hasSize(3);
        assertThat(identificadas.get(0)).get().extracting(Bandeira::nome).isEqualTo("Visa");
        assertThat(identificadas.get(1)).isEmpty();
        assertThat(identificadas.get(2)).get().extracting(Bandeira::nome).isEqualTo(MASTERCARD.nome());
        assertThat(violacoes.lista()).singleElement()
                .extracting(Violacao::mensagem)
                .isEqualTo("Cartão 2: bandeira do cartão não aceita.");
    }

    @Test
    void rnCli005_semCartoesDevolveListaVaziaSemViolacao() {
        Violacoes violacoes = new Violacoes();

        assertThat(regra.identificar(comCartoes(List.of()), violacoes, bandeiras())).isEmpty();
        assertThat(violacoes.lista()).isEmpty();
    }

    private List<Violacao> violacoes(NovoCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.identificar(alvo, violacoes, bandeiras());
        return violacoes.lista();
    }

    private List<Violacao> violacoes(NovoCartao alvo) {
        Violacoes violacoes = new Violacoes();
        regra.identificar(alvo, violacoes, bandeiras());
        return violacoes.lista();
    }
}
