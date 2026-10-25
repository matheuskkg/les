package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.CPF;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.EMAIL;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.MASTERCARD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.SENHA;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.VISA;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comCartoes;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comEnderecos;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comSenha;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.CriarUsuario;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeBandeiraRepositorio;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeClienteRepositorio;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeCodificadorDeSenha;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.regra.CpfUnico;
import br.fatec.les.livraria.clientes.aplicacao.regra.DataNascimentoValida;
import br.fatec.les.livraria.clientes.aplicacao.regra.EmailUnico;
import br.fatec.les.livraria.clientes.aplicacao.regra.TelefoneUnico;
import br.fatec.les.livraria.clientes.aplicacao.regra.ValidadeDoCartao;
import br.fatec.les.livraria.clientes.dominio.calculo.DigitoVerificadorLuhn;
import br.fatec.les.livraria.clientes.dominio.calculo.IdentificarBandeira;
import br.fatec.les.livraria.clientes.dominio.calculo.TamanhoDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.regra.BandeiraRegistrada;
import br.fatec.les.livraria.clientes.dominio.regra.CartaoCompleto;
import br.fatec.les.livraria.clientes.aplicacao.regra.ValidadesDosCartoes;
import br.fatec.les.livraria.clientes.dominio.regra.CartoesCompletos;
import br.fatec.les.livraria.clientes.dominio.regra.NumeroDoCartaoValido;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCartao;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosEndereco;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecosCompletos;
import br.fatec.les.livraria.acesso.dominio.regra.ConfirmacaoDeSenha;
import br.fatec.les.livraria.clientes.dominio.regra.CpfValido;
import br.fatec.les.livraria.clientes.dominio.regra.DadosObrigatoriosCliente;
import br.fatec.les.livraria.clientes.dominio.regra.EmailValido;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoDoCliente;
import br.fatec.les.livraria.acesso.dominio.regra.SenhaForte;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;

/** RF-CLI-001: autocadastro do cliente, com todas as regras e gravação em uma transação (DEC-08). */
class CadastrarClienteTest {

    private final FakeClienteRepositorio clientes = new FakeClienteRepositorio();
    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final FakeBandeiraRepositorio bandeiras = new FakeBandeiraRepositorio()
            .comBandeiras(VISA, MASTERCARD);
    private final FakeCodificadorDeSenha senhas = new FakeCodificadorDeSenha();
    private final ContasEmUso contas = contasEmUso(usuarios);

    private CadastrarCliente caso;

    @BeforeEach
    void montar() {
        caso = new CadastrarCliente(
                new DadosObrigatoriosCliente(),
                new DataNascimentoValida(relogio()),
                new EnderecoDoCliente(),
                new EnderecosCompletos(new EnderecoCompleto()),
                new TamanhosMaximosCliente(new TamanhosMaximosEndereco(), new TamanhosMaximosCartao()),
                new SenhaForte(),
                new ConfirmacaoDeSenha(),
                new CpfValido(),
                new EmailValido(),
                new CartoesCompletos(new CartaoCompleto()),
                new ValidadesDosCartoes(new ValidadeDoCartao(relogio())),
                new CpfUnico(clientes, contas),
                new EmailUnico(contas),
                new TelefoneUnico(contas),
                new BandeiraRegistrada(new IdentificarBandeira()),
                new NumeroDoCartaoValido(new IdentificarBandeira(), new TamanhoDoCartao(),
                        new DigitoVerificadorLuhn()),
                new CriarUsuario(usuarios, senhas),
                clientes,
                bandeiras);
    }

    @Test
    void rfCli001_cadastroValidoCriaUsuarioECliente() {
        Long id = caso.executar(padrao());

        assertThat(id).isNotNull();
        assertThat(usuarios.salvos).singleElement()
                .satisfies(usuario -> {
                    assertThat(usuario.getEmail()).isEqualTo(EMAIL);
                    assertThat(usuario.getTelefone().digitos()).isEqualTo("11988887001");
                    assertThat(usuario.getSenhaHash()).isEqualTo("hash:" + SENHA);
                    assertThat(usuario.getPapel().name()).isEqualTo("CLIENTE");
                });
        assertThat(senhas.codificadas).containsExactly(SENHA);

        assertThat(clientes.salvos).singleElement().satisfies(cliente -> {
            assertThat(cliente.getCpf().numero()).isEqualTo(CPF);
            assertThat(cliente.getUsuarioId()).isEqualTo(usuarios.salvos.getFirst().getId());
            assertThat(cliente.getEnderecos()).hasSize(1);
            assertThat(cliente.getCartoes()).singleElement()
                    .extracting(Cartao::preferencial)
                    .isEqualTo(true);
            assertThat(cliente.getEnderecos().getFirst().pais()).isEqualTo("Brasil");
        });
    }

    @Test
    void rfCli001_cadastroInvalidoAcumulaTodasAsViolacoesESemGravar() {
        NovoCliente invalido = new NovoCliente(" ", null, null, "123", "sem-arroba",
                null, "1", "12", "abc", "outra", List.of(), List.of());

        ExcecaoDeNegocio erro = catchThrowableOfType(() -> caso.executar(invalido), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).extracting(Violacao::codigo)
                .contains("RN-CLI-006", "RN-CLI-001", "RNF-CLI-001");
        assertThat(erro.getViolacoes()).hasSizeGreaterThan(5);
        assertThat(usuarios.salvos).isEmpty();
        assertThat(clientes.salvos).isEmpty();
    }

    @Test
    void rfCli001_cpfEmailETelefoneEmUsoViolamAsUnicidades() {
        clientes.usuariosPorCpf.put(CPF, List.of(2L));
        usuarios.idsEmUso.add(2L);
        usuarios.emailsEmUso.add(EMAIL);
        usuarios.telefonesEmUso.add("11988887001");

        ExcecaoDeNegocio erro = catchThrowableOfType(() -> caso.executar(padrao()), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).extracting(Violacao::codigo)
                .containsOnly("RF-CLI-001")
                .hasSize(3);
    }

    @Test
    void rfCli001_dadosDeContaEncerradaSaoAceitos() {
        clientes.usuariosPorCpf.put(CPF, List.of(9L));

        assertThat(caso.executar(padrao())).isNotNull();
        assertThat(usuarios.salvos).hasSize(1);
    }

    @Test
    void rfCli001_semEnderecoGeraUmaUnicaViolacao() {
        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(comEnderecos(List.of())), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes())
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-001");
                    assertThat(violacao.mensagem()).contains("RN-CLI-001", "RN-CLI-002");
                });
    }

    @Test
    void rnfCli001_senhaEmBrancoEhReportadaUmaUnicaVez() {
        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(comSenha("   ")), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).singleElement().satisfies(violacao -> {
            assertThat(violacao.codigo()).isEqualTo("RN-CLI-006");
            assertThat(violacao.mensagem()).isEqualTo("Informe a senha do cliente.");
        });
    }

    @Test
    void rnCli004_luhnInvalidoNoCadastroEhNumerado() {
        NovoCartao luhnInvalido = new NovoCartao("4111111111111112", "ANA SOUZA", cartao().validade());

        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(comCartoes(List.of(cartao(), luhnInvalido))), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).singleElement().satisfies(violacao -> {
            assertThat(violacao.codigo()).isEqualTo("RN-CLI-004");
            assertThat(violacao.mensagem()).isEqualTo("Cartão 2: número do cartão inválido.");
        });
    }

    @Test
    void rnCli005_bandeirasIdentificadasFicamAlinhadasAosCartoes() {
        NovoCartao mastercard = new NovoCartao("5555555555554444", "ANA SOUZA", cartao().validade());

        caso.executar(comCartoes(List.of(cartao(), mastercard)));

        assertThat(clientes.salvos).singleElement().satisfies(cliente ->
                assertThat(cliente.getCartoes()).extracting(cartao -> cartao.bandeira().nome())
                        .containsExactly(VISA.nome(), MASTERCARD.nome()));
    }
}
