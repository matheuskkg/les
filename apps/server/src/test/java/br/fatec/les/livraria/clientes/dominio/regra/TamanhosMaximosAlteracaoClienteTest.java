package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.alteracao;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.DadosCadastraisCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.junit.jupiter.api.Test;

/** RF-CLI-002, RN-CLI-006: limite da coluna {@code cliente.nome} também na alteração. */
class TamanhosMaximosAlteracaoClienteTest {

    private final TamanhosMaximosAlteracaoCliente regra = new TamanhosMaximosAlteracaoCliente();

    @Test
    void rnCli006_nomeNoLimiteEhAceito() {
        assertThat(violacoes(comNome("a".repeat(TamanhosMaximosCliente.NOME)))).isEmpty();
    }

    @Test
    void rnCli006_nomeAcimaDoLimiteEhRejeitado() {
        assertThat(violacoes(comNome("a".repeat(TamanhosMaximosCliente.NOME + 1))))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-006");
                    assertThat(violacao.mensagem())
                            .contains("nome do cliente")
                            .contains(String.valueOf(TamanhosMaximosCliente.NOME));
                });
    }

    @Test
    void rnCli006_nomeComEspacosNoFimEhAvaliadoSemEles() {
        String nome = "a".repeat(TamanhosMaximosCliente.NOME) + "   ";

        assertThat(violacoes(comNome(nome))).isEmpty();
    }

    private static DadosAlteradosDoCliente comNome(String nome) {
        DadosAlteradosDoCliente padrao = alteracao();
        return new DadosAlteradosDoCliente(
                padrao.usuarioId(),
                nome,
                Genero.FEMININO,
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                TipoTelefone.CELULAR,
                padrao.ddd(),
                padrao.numero());
    }

    private java.util.List<Violacao> violacoes(DadosCadastraisCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
