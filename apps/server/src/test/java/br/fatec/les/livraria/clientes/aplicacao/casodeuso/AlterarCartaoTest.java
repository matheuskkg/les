package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.MASTERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.VALIDADE_FUTURA;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.VISA;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.clienteComCartoes;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeBandeiraRepositorio;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeClienteRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.regra.ValidadeDoCartao;
import br.fatec.les.livraria.clientes.dominio.calculo.DigitoVerificadorLuhn;
import br.fatec.les.livraria.clientes.dominio.calculo.IdentificarBandeira;
import br.fatec.les.livraria.clientes.dominio.calculo.TamanhoDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.regra.BandeiraRegistrada;
import br.fatec.les.livraria.clientes.dominio.regra.CartaoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.NumeroDoCartaoValido;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCartao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RF-CLI-007, RN-CLI-004, RN-CLI-005 (DEC-18): alteração de um cartão do cliente autenticado. */
class AlterarCartaoTest {

    private final FakeClienteRepositorio clientes = new FakeClienteRepositorio();
    private final FakeBandeiraRepositorio bandeiras = new FakeBandeiraRepositorio().comBandeiras(VISA, MASTERCARD);

    private AlterarCartao caso;

    @BeforeEach
    void montar() {
        IdentificarBandeira identificarBandeira = new IdentificarBandeira();
        caso =
                new AlterarCartao(
                        new CartaoCompleto(),
                        new TamanhosMaximosCartao(),
                        new ValidadeDoCartao(relogio()),
                        new BandeiraRegistrada(identificarBandeira),
                        new NumeroDoCartaoValido(
                                identificarBandeira, new TamanhoDoCartao(), new DigitoVerificadorLuhn()),
                        new ClienteDoUsuario(clientes),
                        clientes,
                        bandeiras);
    }

    @Test
    void rnCli004_alterarSoONomeMantemNumeroEBandeiraSemRevalidarOLuhn() {
        // Número que hoje seria recusado pela RN-CLI-004 (sem bandeira e com Luhn inválido): como não foi informado
        // na alteração, ele e a bandeira gravada são mantidos, sem reidentificação nem nova validação.
        Cartao atual = new Cartao(1L, "9999999999999999", "ANA SOUZA", VISA, VALIDADE_FUTURA, true);
        clientes.salvos.add(clienteComCartoes(10L, 2L, atual));

        Cartao alterado =
                caso.executar(2L, new AlteracaoDeCartao(1L, new NovoCartao("", "ANA S SOUZA", VALIDADE_FUTURA)));

        assertThat(alterado.numero()).isEqualTo("9999999999999999");
        assertThat(alterado.nomeImpresso()).isEqualTo("ANA S SOUZA");
        assertThat(alterado.bandeira()).isEqualTo(VISA);
        assertThat(alterado.preferencial()).isTrue();
    }

    @Test
    void rnCli005_numeroNovoEhIdentificadoDeNovo() {
        Cartao atual = new Cartao(1L, "4111111111111111", "ANA SOUZA", VISA, VALIDADE_FUTURA, true);
        clientes.salvos.add(clienteComCartoes(10L, 2L, atual));

        Cartao alterado =
                caso.executar(
                        2L,
                        new AlteracaoDeCartao(1L, new NovoCartao("5555555555554444", "ANA SOUZA", VALIDADE_FUTURA)));

        assertThat(alterado.numero()).isEqualTo("5555555555554444");
        assertThat(alterado.bandeira()).isEqualTo(MASTERCARD);
    }
}
