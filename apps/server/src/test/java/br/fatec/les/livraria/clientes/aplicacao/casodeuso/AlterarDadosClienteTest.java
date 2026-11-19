package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.CPF;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.EMAIL;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.alteracao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.cliente;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.AlterarContato;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ConsultarContato;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeClienteRepositorio;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.CalcularRanking;
import br.fatec.les.livraria.clientes.aplicacao.regra.CpfUnicoNaAlteracao;
import br.fatec.les.livraria.clientes.aplicacao.regra.DataNascimentoValida;
import br.fatec.les.livraria.clientes.aplicacao.regra.EmailUnicoNaAlteracao;
import br.fatec.les.livraria.clientes.aplicacao.regra.TelefoneUnicoNaAlteracao;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.regra.CpfValido;
import br.fatec.les.livraria.clientes.dominio.regra.DadosObrigatoriosAlteracaoCliente;
import br.fatec.les.livraria.clientes.dominio.regra.EmailValido;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosAlteracaoCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;

/** RF-CLI-002, RN-CLI-006, DEC-07, DEC-08, DEC-10: alteração dos dados do próprio cliente. */
class AlterarDadosClienteTest {

    private final FakeClienteRepositorio clientes = new FakeClienteRepositorio();
    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();

    private AlterarDadosCliente caso;

    @BeforeEach
    void montar() {
        clientes.salvos.add(cliente(1L, 2L));
        usuarios.salvos.add(usuario(2L, EMAIL, "11988887001", Papel.CLIENTE));
        var contas = contasEmUso(usuarios);
        caso = new AlterarDadosCliente(
                new DadosObrigatoriosAlteracaoCliente(),
                new TamanhosMaximosAlteracaoCliente(),
                new DataNascimentoValida(relogio()),
                new CpfValido(),
                new EmailValido(),
                new CpfUnicoNaAlteracao(clientes, contas),
                new EmailUnicoNaAlteracao(contas),
                new TelefoneUnicoNaAlteracao(contas),
                clientes,
                new AlterarContato(usuarios),
                new ConsultarMeusDados(clientes, new ConsultarContato(usuarios),
                        new CalcularRanking((ids, desde) -> Map.of(), relogio())));
    }

    @Test
    void rfCli002_alteraCadastroEContatoEmUmaOperacao() {
        DadosAlteradosDoCliente comando = new DadosAlteradosDoCliente(2L, "Ana Souza Lima", Genero.FEMININO,
                LocalDate.of(1991, 5, 20), "11144477735", "ana.lima@livraria.test",
                TipoTelefone.FIXO, "21", "98888-7002");

        DadosDoCliente dados = caso.executar(comando);

        assertThat(dados.id()).isEqualTo(1L);
        assertThat(dados.nome()).isEqualTo("Ana Souza Lima");
        assertThat(dados.dataNascimento()).isEqualTo(LocalDate.of(1991, 5, 20));
        assertThat(dados.cpf()).isEqualTo("11144477735");
        assertThat(dados.email()).isEqualTo("ana.lima@livraria.test");
        assertThat(dados.tipoTelefone()).isEqualTo(TipoTelefone.FIXO);
        assertThat(dados.ddd()).isEqualTo("21");
        assertThat(dados.numero()).isEqualTo("988887002");
        assertThat(clientes.salvos.getLast().getNome()).isEqualTo("Ana Souza Lima");
        assertThat(usuarios.salvos.getLast().getEmail()).isEqualTo("ana.lima@livraria.test");
    }

    @Test
    void rfCli002_cpfOmitidoMantemOCpfAtual() {
        DadosDoCliente dados = caso.executar(comCpf(null));

        assertThat(dados.cpf()).isEqualTo(CPF);
    }

    @Test
    void rfCli002_cpfEmBrancoMantemOCpfAtual() {
        DadosDoCliente dados = caso.executar(comCpf("   "));

        assertThat(dados.cpf()).isEqualTo(CPF);
    }

    @Test
    void rfCli002_manterOProprioEmailETelefoneNaoBloqueia() {
        usuarios.donosDosEmails.put(EMAIL, 2L);
        usuarios.donosDosTelefones.put("11988887001", 2L);

        assertThat(caso.executar(alteracao())).isNotNull();
    }

    @Test
    void rfCli002_emailETelefoneDeOutraContaEmUsoAcumulamViolacoes() {
        usuarios.donosDosEmails.put("bruno@livraria.test", 3L);
        usuarios.donosDosTelefones.put("21988887002", 3L);
        DadosAlteradosDoCliente comando = new DadosAlteradosDoCliente(2L, "Ana Souza", Genero.FEMININO,
                LocalDate.of(1990, 3, 14), CPF, "bruno@livraria.test", TipoTelefone.CELULAR, "21", "988887002");

        ExcecaoDeNegocio erro = catchThrowableOfType(ExcecaoDeNegocio.class, () -> caso.executar(comando));

        assertThat(erro.getViolacoes()).extracting(Violacao::codigo)
                .containsOnly("RF-CLI-002")
                .hasSize(2);
    }

    @Test
    void rfCli002_dadosInvalidosAcumulamViolacoesESemGravar() {
        int clientesAntes = clientes.salvos.size();
        int usuariosAntes = usuarios.salvos.size();
        DadosAlteradosDoCliente invalido = new DadosAlteradosDoCliente(2L, " ", null, null, "123",
                "sem-arroba", null, "1", "12");

        ExcecaoDeNegocio erro = catchThrowableOfType(ExcecaoDeNegocio.class, () -> caso.executar(invalido));

        assertThat(erro.getViolacoes()).extracting(Violacao::codigo)
                .containsOnly("RN-CLI-006")
                .hasSize(8);
        assertThat(clientes.salvos).hasSize(clientesAntes);
        assertThat(usuarios.salvos).hasSize(usuariosAntes);
    }

    @Test
    void rfCli002_clienteSemCadastroNaoEhEncontrado() {
        clientes.salvos.clear();

        assertThat(catchThrowableOfType(ExcecaoDeRecursoNaoEncontrado.class, () -> caso.executar(alteracao())))
                .hasMessage("Cliente não encontrado.");
    }

    private static DadosAlteradosDoCliente comCpf(String cpf) {
        DadosAlteradosDoCliente padrao = alteracao();
        return new DadosAlteradosDoCliente(padrao.usuarioId(), padrao.nome(), padrao.genero(),
                padrao.dataNascimento(), cpf, padrao.email(), padrao.tipoTelefone(), padrao.ddd(),
                padrao.numero());
    }
}
