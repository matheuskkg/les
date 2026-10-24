package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

/** RNF-CLI-002: confirmação de senha. */
class ConfirmacaoDeSenhaTest {

    private final ConfirmacaoDeSenha regra = new ConfirmacaoDeSenha();

    @Test
    void rnfCli002_senhasIguaisNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnfCli002_confirmacaoDiferenteViolaARegra() {
        NovoCliente padrao = padrao();
        NovoCliente diferente = new NovoCliente(padrao.nome(), padrao.genero(),
                padrao.dataNascimento(), padrao.cpf(), padrao.email(), padrao.tipoTelefone(),
                padrao.ddd(), padrao.numero(), padrao.senha(), "Outra@123", padrao.enderecos(),
                padrao.cartoes());

        assertThat(violacoes(diferente, regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RNF-CLI-002");
    }

    @Test
    void rnfCli002_confirmacaoAusenteViolaARegra() {
        NovoCliente padrao = padrao();
        NovoCliente semConfirmacao = new NovoCliente(padrao.nome(), padrao.genero(),
                padrao.dataNascimento(), padrao.cpf(), padrao.email(), padrao.tipoTelefone(),
                padrao.ddd(), padrao.numero(), padrao.senha(), null, padrao.enderecos(),
                padrao.cartoes());

        assertThat(violacoes(semConfirmacao, regra)).isNotEmpty();
    }
}
