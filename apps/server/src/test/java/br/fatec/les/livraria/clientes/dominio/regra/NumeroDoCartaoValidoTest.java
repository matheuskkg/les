package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.bandeiras;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comCartoes;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.dominio.calculo.DigitoVerificadorLuhn;
import br.fatec.les.livraria.clientes.dominio.calculo.IdentificarBandeira;
import br.fatec.les.livraria.clientes.dominio.calculo.TamanhoDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.junit.jupiter.api.Test;

import java.util.List;

/** RN-CLI-004 (DEC-18): tamanho do número conforme a bandeira e dígito verificador (Luhn). */
class NumeroDoCartaoValidoTest {

    private final NumeroDoCartaoValido regra =
            new NumeroDoCartaoValido(new IdentificarBandeira(), new TamanhoDoCartao(), new DigitoVerificadorLuhn());

    @Test
    void rnCli004_numeroValidoDaBandeiraNaoViolaARegra() {
        assertThat(violacoes(cartao())).isEmpty();
    }

    @Test
    void rnCli004_digitoVerificadorInvalidoViolaARegra() {
        NovoCartao luhnInvalido = new NovoCartao("4111111111111112", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(luhnInvalido))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-004");
                    assertThat(violacao.mensagem()).isEqualTo("Número do cartão inválido.");
                });
    }

    @Test
    void rnCli004_tamanhoIncompativelComABandeiraViolaARegra() {
        NovoCartao mastercardDeTrezeDigitos = new NovoCartao("5555555555554", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(mastercardDeTrezeDigitos))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-004");
                    assertThat(violacao.mensagem()).isEqualTo("Número do cartão inválido.");
                });
    }

    @Test
    void rnCli004_digitoVerificadorInvalidoNoCadastroEhNumerado() {
        NovoCartao luhnInvalido = new NovoCartao("4111111111111112", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(comCartoes(List.of(cartao(), luhnInvalido))))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-004");
                    assertThat(violacao.mensagem()).isEqualTo("Cartão 2: número do cartão inválido.");
                });
    }

    @Test
    void rnCli004_numeroSemBandeiraFicaParaARegraRnCli005() {
        NovoCartao desconhecido = new NovoCartao("9999999999999999", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(desconhecido)).isEmpty();
    }

    @Test
    void rnCli004_numeroComMascaraEhAvaliado() {
        NovoCartao mascarado = new NovoCartao("4111 1111 1111 1112", "ANA SOUZA", cartao().validade());

        assertThat(violacoes(mascarado))
                .singleElement()
                .extracting(Violacao::mensagem)
                .isEqualTo("Número do cartão inválido.");
    }

    private List<Violacao> violacoes(NovoCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes, bandeiras());
        return violacoes.lista();
    }

    private List<Violacao> violacoes(NovoCartao alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes, bandeiras());
        return violacoes.lista();
    }
}
