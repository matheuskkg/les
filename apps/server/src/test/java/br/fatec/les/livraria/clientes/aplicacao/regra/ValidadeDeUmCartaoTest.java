package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;

/** DEC-06: a regra de um cartão só, usada na inclusão e na alteração em "Minha conta" (relógio em 2026-10). */
class ValidadeDeUmCartaoTest {

    private final ValidadeDoCartao regra = new ValidadeDoCartao(relogio());

    @Test
    void dec06_mesCorrenteEhAceito() {
        assertThat(violacoes(comValidade(YearMonth.of(2026, 10)), regra)).isEmpty();
    }

    @Test
    void dec06_mesAnteriorEhRecusadoSemPrefixoDeItem() {
        assertThat(violacoes(comValidade(YearMonth.of(2026, 9)), regra))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo("RN-CLI-004");
                    assertThat(violacao.mensagem()).doesNotStartWith("Cartão 1");
                });
    }

    private static NovoCartao comValidade(YearMonth validade) {
        NovoCartao padrao = cartao();
        return new NovoCartao(padrao.numero(), padrao.nomeImpresso(), validade);
    }
}
