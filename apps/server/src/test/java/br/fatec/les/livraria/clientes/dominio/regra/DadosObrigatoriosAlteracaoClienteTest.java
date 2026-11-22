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

/** RF-CLI-002, RN-CLI-006, DEC-10: dados obrigatórios da alteração do cadastro do cliente. */
class DadosObrigatoriosAlteracaoClienteTest {

    private final DadosObrigatoriosAlteracaoCliente regra = new DadosObrigatoriosAlteracaoCliente();

    @Test
    void rnCli006_dadosCompletosNaoTemViolacao() {
        assertThat(violacoes(alteracao())).isEmpty();
    }

    @Test
    void rnCli006_cpfOmitidoNaoEhObrigatorio() {
        DadosAlteradosDoCliente semCpf =
                new DadosAlteradosDoCliente(
                        2L,
                        "Ana Souza",
                        Genero.FEMININO,
                        alteracao().dataNascimento(),
                        null,
                        alteracao().email(),
                        TipoTelefone.CELULAR,
                        alteracao().ddd(),
                        alteracao().numero());

        assertThat(violacoes(semCpf)).isEmpty();
    }

    @Test
    void rnCli006_camposObrigatoriosEmBrancoAcumulamViolacoes() {
        DadosAlteradosDoCliente vazio = new DadosAlteradosDoCliente(2L, " ", null, null, null, " ", null, null, null);

        assertThat(violacoes(vazio)).hasSize(7).allMatch(violacao -> violacao.codigo().equals("RN-CLI-006"));
    }

    @Test
    void rnCli006_dddENumeroInvalidos() {
        DadosAlteradosDoCliente invalido =
                new DadosAlteradosDoCliente(
                        2L,
                        "Ana Souza",
                        Genero.FEMININO,
                        alteracao().dataNascimento(),
                        null,
                        alteracao().email(),
                        TipoTelefone.CELULAR,
                        "1",
                        "123");

        assertThat(violacoes(invalido))
                .extracting(Violacao::mensagem)
                .anyMatch(mensagem -> mensagem.contains("DDD"))
                .anyMatch(mensagem -> mensagem.contains("telefone"));
    }

    private java.util.List<Violacao> violacoes(DadosCadastraisCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
