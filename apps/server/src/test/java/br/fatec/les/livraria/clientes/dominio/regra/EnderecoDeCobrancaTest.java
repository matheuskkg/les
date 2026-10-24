package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

/** RN-CLI-001: ao menos um endereço atende a cobrança, escolhida na compra (DEC-10). */
class EnderecoDeCobrancaTest {

    private final EnderecoDeCobranca regra = new EnderecoDeCobranca();

    @Test
    void rnCli001_comUmEnderecoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli001_semEnderecoViolaARegra() {
        NovoCliente semEndereco = semEndereco();

        assertThat(violacoes(semEndereco, regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RN-CLI-001");
    }

    private static NovoCliente semEndereco() {
        NovoCliente padrao = padrao();
        return new NovoCliente(padrao.nome(), padrao.genero(), padrao.dataNascimento(), padrao.cpf(),
                padrao.email(), padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), padrao.senha(),
                padrao.confirmacaoSenha(), List.of(), padrao.cartoes());
    }
}
