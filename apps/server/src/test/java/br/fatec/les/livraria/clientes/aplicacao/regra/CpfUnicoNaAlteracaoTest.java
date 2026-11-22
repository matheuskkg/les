package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.CPF;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.alteracao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeClienteRepositorio;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.junit.jupiter.api.Test;

import java.util.List;

/** RF-CLI-002, DEC-10: unicidade do CPF na alteração, ignorando a própria conta. */
class CpfUnicoNaAlteracaoTest {

    private final FakeClienteRepositorio clientes = new FakeClienteRepositorio();
    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final CpfUnicoNaAlteracao regra = new CpfUnicoNaAlteracao(clientes, contasEmUso(usuarios));

    @Test
    void rfCli002_mesmoCpfDaPropriaContaNaoViolaARegra() {
        clientes.usuariosPorCpf.put(CPF, List.of(2L));
        usuarios.idsEmUso.add(2L);

        assertThat(violacoes(comCpf(2L, CPF))).isEmpty();
    }

    @Test
    void rfCli002_cpfDeOutraContaEmUsoViolaARegra() {
        clientes.usuariosPorCpf.put(CPF, List.of(3L));
        usuarios.idsEmUso.add(3L);

        assertThat(violacoes(comCpf(2L, CPF))).singleElement().extracting(Violacao::codigo).isEqualTo("RF-CLI-002");
    }

    @Test
    void rfCli002_cpfDeContaEncerradaEhAceito() {
        clientes.usuariosPorCpf.put(CPF, List.of(3L));

        assertThat(violacoes(comCpf(2L, CPF))).isEmpty();
    }

    @Test
    void rfCli002_cpfOmitidoNaoEhValidadoPorEstaRegra() {
        clientes.usuariosPorCpf.put(CPF, List.of(3L));
        usuarios.idsEmUso.add(3L);

        assertThat(violacoes(comCpf(2L, null))).isEmpty();
        assertThat(violacoes(comCpf(2L, "   "))).isEmpty();
    }

    @Test
    void rfCli002_cpfInvalidoNaoEhValidadoPorEstaRegra() {
        clientes.usuariosPorCpf.put("123", List.of(3L));
        usuarios.idsEmUso.add(3L);

        assertThat(violacoes(comCpf(2L, "123"))).isEmpty();
    }

    private static DadosAlteradosDoCliente comCpf(Long usuarioId, String cpf) {
        DadosAlteradosDoCliente padrao = alteracao();
        return new DadosAlteradosDoCliente(
                usuarioId,
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                cpf,
                padrao.email(),
                TipoTelefone.CELULAR,
                padrao.ddd(),
                padrao.numero());
    }

    private List<Violacao> violacoes(DadosAlteradosDoCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
