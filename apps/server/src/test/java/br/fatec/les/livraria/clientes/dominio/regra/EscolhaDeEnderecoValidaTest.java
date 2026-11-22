package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.endereco;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;

import org.junit.jupiter.api.Test;

/** RF-VEN-005: a compra usa um endereço cadastrado ou um novo, nunca os dois nem nenhum. */
class EscolhaDeEnderecoValidaTest {

    private final EscolhaDeEnderecoValida regra = new EscolhaDeEnderecoValida();

    @Test
    void rfVen005_enderecoCadastradoEhAceito() {
        assertThat(violacoes(new EscolhaDeEndereco(1L, null), regra)).isEmpty();
    }

    @Test
    void rfVen005_enderecoNovoEhAceito() {
        assertThat(violacoes(new EscolhaDeEndereco(null, endereco()), regra)).isEmpty();
    }

    @Test
    void rfVen005_nenhumEnderecoEhRecusado() {
        assertThat(violacoes(new EscolhaDeEndereco(null, null), regra))
                .singleElement()
                .satisfies(violacao -> assertThat(violacao.codigo()).isEqualTo("RF-VEN-005"));
        assertThat(violacoes(null, regra)).hasSize(1);
    }

    @Test
    void rfVen005_cadastradoENovoJuntosSaoRecusados() {
        assertThat(violacoes(new EscolhaDeEndereco(1L, endereco()), regra)).hasSize(1);
    }
}
