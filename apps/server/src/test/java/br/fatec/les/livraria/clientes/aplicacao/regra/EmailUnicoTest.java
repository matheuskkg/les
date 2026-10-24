package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.EMAIL;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

/** RF-CLI-001: unicidade do e-mail entre as contas em uso (DEC-10). */
class EmailUnicoTest {

    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final EmailUnico regra = new EmailUnico(
            br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso(usuarios));

    @Test
    void rfCli001_emailDeContaEmUsoViolaARegra() {
        usuarios.emailsEmUso.add(EMAIL);

        assertThat(violacoes(padrao(), regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RF-CLI-001");
    }

    @Test
    void rfCli001_emailLivreNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rfCli001_emailEhComparadoSemDiferenciarMaiusculas() {
        usuarios.emailsEmUso.add(EMAIL); // no banco o e-mail fica em minúsculas

        assertThat(violacoes(comEmail("ANA.SOUZA@LIVRARIA.TEST"), regra)).hasSize(1);
    }

    private static NovoCliente comEmail(String email) {
        var padrao = padrao();
        return new NovoCliente(padrao.nome(), padrao.genero(), padrao.dataNascimento(), padrao.cpf(), email,
                padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), padrao.senha(), padrao.confirmacaoSenha(),
                padrao.enderecos(), padrao.cartoes());
    }
}
