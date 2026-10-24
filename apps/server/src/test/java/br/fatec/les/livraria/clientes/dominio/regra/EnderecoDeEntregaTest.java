package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;

/** RN-CLI-002: ao menos um endereço atende a entrega, escolhida na compra (DEC-10). */
class EnderecoDeEntregaTest {

    private final EnderecoDeEntrega regra = new EnderecoDeEntrega();

    @Test
    void rnCli002_comUmEnderecoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli002_semEnderecoViolaARegra() {
        NovoCliente padrao = padrao();
        NovoCliente semEndereco = new NovoCliente(padrao.nome(), padrao.genero(),
                padrao.dataNascimento(), padrao.cpf(), padrao.email(), padrao.tipoTelefone(),
                padrao.ddd(), padrao.numero(), padrao.senha(), padrao.confirmacaoSenha(),
                List.of(), padrao.cartoes());

        assertThat(violacoes(semEndereco, regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RN-CLI-002");
    }
}
