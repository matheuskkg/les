package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

/** RF-VEN-006: a compra usa um cartão cadastrado ou um novo, nunca os dois nem nenhum. */
class EscolhaDeCartaoValidaTest {

    private final EscolhaDeCartaoValida regra = new EscolhaDeCartaoValida();
    private final NovoCartao novo = new NovoCartao("4111111111111111", "ANA", YearMonth.of(2030, 1));

    @Test
    void rfVen006_cartaoCadastradoOuNovoEhAceito() {
        assertThat(violacoes(new EscolhaDeCartao(1L, null), regra)).isEmpty();
        assertThat(violacoes(new EscolhaDeCartao(null, novo), regra)).isEmpty();
    }

    @Test
    void rfVen006_nenhumOuOsDoisSaoRecusados() {
        assertThat(violacoes(new EscolhaDeCartao(null, null), regra))
                .singleElement()
                .satisfies(violacao -> assertThat(violacao.codigo()).isEqualTo("RF-VEN-006"));
        assertThat(violacoes(new EscolhaDeCartao(1L, novo), regra)).hasSize(1);
        assertThat(violacoes(null, regra)).hasSize(1);
    }
}
