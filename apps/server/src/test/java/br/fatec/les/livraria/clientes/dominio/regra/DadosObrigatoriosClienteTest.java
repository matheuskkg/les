package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

/** RN-CLI-006: dados obrigatórios do cadastro do cliente. */
class DadosObrigatoriosClienteTest {

    private final DadosObrigatoriosCliente regra = new DadosObrigatoriosCliente();

    @Test
    void rnCli006_dadosCompletosNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli006_camposObrigatoriosEmBrancoAcumulamViolacoes() {
        NovoCliente vazio = new NovoCliente(" ", null, null, " ", null, null, null, null, null, null,
                List.of(), List.of());

        assertThat(violacoes(vazio, regra))
                .hasSize(10)
                .allMatch(violacao -> violacao.codigo().equals("RN-CLI-006"));
    }

    @Test
    void rnCli006_dddENumeroInvalidos() {
        NovoCliente padrao = padrao();
        NovoCliente invalido = new NovoCliente(padrao.nome(), padrao.genero(), padrao.dataNascimento(),
                padrao.cpf(), padrao.email(), padrao.tipoTelefone(), "1", "123", padrao.senha(),
                padrao.confirmacaoSenha(), padrao.enderecos(), padrao.cartoes());

        assertThat(violacoes(invalido, regra))
                .anyMatch(violacao -> violacao.mensagem().contains("DDD"));
        assertThat(violacoes(invalido, regra))
                .anyMatch(violacao -> violacao.mensagem().contains("telefone"));
    }

    @Test
    void rnCli006_semEndereco() {
        NovoCliente padrao = padrao();
        NovoCliente semEndereco = new NovoCliente(padrao.nome(), padrao.genero(),
                padrao.dataNascimento(), padrao.cpf(), padrao.email(), padrao.tipoTelefone(),
                padrao.ddd(), padrao.numero(), padrao.senha(), padrao.confirmacaoSenha(),
                List.of(), padrao.cartoes());

        assertThat(violacoes(semEndereco, regra))
                .anyMatch(violacao -> violacao.mensagem().contains("endereço"));
    }

    @Test
    void rnCli006_generoObrigatorio() {
        NovoCliente padrao = padrao();
        NovoCliente semGenero = new NovoCliente(padrao.nome(), (Genero) null, padrao.dataNascimento(),
                padrao.cpf(), padrao.email(), padrao.tipoTelefone(), padrao.ddd(), padrao.numero(),
                padrao.senha(), padrao.confirmacaoSenha(), padrao.enderecos(), padrao.cartoes());

        assertThat(violacoes(semGenero, regra))
                .anyMatch(violacao -> violacao.mensagem().contains("gênero"));
    }
}
