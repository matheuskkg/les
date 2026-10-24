package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

/** RN-CLI-006: e-mail com formato válido, conferido pelo value object {@code Email}. */
class EmailValidoTest {

    private final EmailValido regra = new EmailValido();

    @Test
    void rnCli006_emailValidoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli006_emailSemArrobaEhRejeitado() {
        assertThat(violacoes(comEmail("ana.souza"), regra))
                .singleElement()
                .extracting(violacao -> violacao.mensagem())
                .isEqualTo("E-mail inválido.");
    }

    private static NovoCliente comEmail(String email) {
        NovoCliente padrao = padrao();
        return new NovoCliente(padrao.nome(), padrao.genero(), padrao.dataNascimento(), padrao.cpf(),
                email, padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), padrao.senha(),
                padrao.confirmacaoSenha(), padrao.enderecos(), padrao.cartoes());
    }
}
