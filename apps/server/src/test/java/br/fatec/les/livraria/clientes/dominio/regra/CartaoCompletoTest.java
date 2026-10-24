package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

/** RN-CLI-004: composição do cartão (sem código de segurança, DEC-10). */
class CartaoCompletoTest {

    private final CartaoCompleto regra = new CartaoCompleto();

    @Test
    void rnCli004_cartaoCompletoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli004_cartaoSemCamposObrigatorios() {
        NovoCartao vazio = new NovoCartao(null, " ", null, null);

        assertThat(violacoes(comCartao(vazio), regra))
                .hasSize(4)
                .allMatch(violacao -> violacao.codigo().equals("RN-CLI-004"));
    }

    @Test
    void rnCli004_numeroComMenosDeTrezeDigitos() {
        NovoCartao numeroCurto = new NovoCartao("1234", cartao().nomeImpresso(), cartao().bandeiraId(),
                cartao().validade());

        assertThat(violacoes(comCartao(numeroCurto), regra))
                .anyMatch(violacao -> violacao.mensagem().contains("número"));
    }

    @Test
    void rnCli004_listaDeCartoesVaziaNaoTemViolacao() {
        NovoCliente padrao = padrao();
        NovoCliente semCartoes = new NovoCliente(padrao.nome(), padrao.genero(),
                padrao.dataNascimento(), padrao.cpf(), padrao.email(), padrao.tipoTelefone(),
                padrao.ddd(), padrao.numero(), padrao.senha(), padrao.confirmacaoSenha(),
                padrao.enderecos(), List.of());

        assertThat(violacoes(semCartoes, regra)).isEmpty();
    }

    private static NovoCliente comCartao(NovoCartao cartao) {
        NovoCliente padrao = padrao();
        return new NovoCliente(padrao.nome(), padrao.genero(), padrao.dataNascimento(), padrao.cpf(),
                padrao.email(), padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), padrao.senha(),
                padrao.confirmacaoSenha(), padrao.enderecos(), List.of(cartao));
    }
}
