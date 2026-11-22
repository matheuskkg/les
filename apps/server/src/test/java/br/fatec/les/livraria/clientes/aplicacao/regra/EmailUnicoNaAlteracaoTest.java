package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.EMAIL;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.alteracao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.junit.jupiter.api.Test;

import java.util.List;

/** RF-CLI-002, DEC-10: unicidade do e-mail na alteração, ignorando a própria conta. */
class EmailUnicoNaAlteracaoTest {

    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final EmailUnicoNaAlteracao regra = new EmailUnicoNaAlteracao(contasEmUso(usuarios));

    @Test
    void rfCli002_mesmoEmailDaPropriaContaNaoViolaARegra() {
        usuarios.donosDosEmails.put(EMAIL, 2L);

        assertThat(violacoes(comEmail(2L, EMAIL))).isEmpty();
    }

    @Test
    void rfCli002_emailDeOutraContaEmUsoViolaARegra() {
        usuarios.donosDosEmails.put(EMAIL, 3L);

        assertThat(violacoes(comEmail(2L, EMAIL))).singleElement().extracting(Violacao::codigo).isEqualTo("RF-CLI-002");
    }

    @Test
    void rfCli002_emailLivreNaoTemViolacao() {
        assertThat(violacoes(comEmail(2L, EMAIL))).isEmpty();
    }

    @Test
    void rfCli002_emailEhComparadoSemDiferenciarMaiusculas() {
        usuarios.donosDosEmails.put(EMAIL, 2L);

        assertThat(violacoes(comEmail(2L, "ANA.SOUZA@LIVRARIA.TEST"))).isEmpty();
    }

    @Test
    void rfCli002_emailEmBrancoOuInvalidoNaoEhValidadoPorEstaRegra() {
        usuarios.emailsEmUso.add(EMAIL);

        assertThat(violacoes(comEmail(2L, " "))).isEmpty();
        assertThat(violacoes(comEmail(2L, "sem-arroba"))).isEmpty();
    }

    private static DadosAlteradosDoCliente comEmail(Long usuarioId, String email) {
        DadosAlteradosDoCliente padrao = alteracao();
        return new DadosAlteradosDoCliente(
                usuarioId,
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                email,
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
