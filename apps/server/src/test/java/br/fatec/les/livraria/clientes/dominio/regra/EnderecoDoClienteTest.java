package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.comEnderecos;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/** RN-CLI-001 e RN-CLI-002: ao menos um endereço atende a cobrança e a entrega, escolhidas na compra (DEC-10). */
class EnderecoDoClienteTest {

    private final EnderecoDoCliente regra = new EnderecoDoCliente();

    @Test
    void rnCli001_comUmEnderecoNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli001_rnCli002_semEnderecoGeraUmaUnicaViolacao() {
        assertThat(violacoes(comEnderecos(List.of()), regra))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-001");
                    assertThat(violacao.mensagem()).contains("RN-CLI-001", "RN-CLI-002", "endereço");
                });
    }
}
