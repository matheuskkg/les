package br.fatec.les.livraria.vendas.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;

import org.junit.jupiter.api.Test;

/** RF-VEN-002: a quantidade pedida para um item é no mínimo 1. */
class QuantidadeDoItemTest {

    private final QuantidadeDoItem regra = new QuantidadeDoItem();

    @Test
    void rfVen002_quantidadePositivaEhAceita() {
        Violacoes violacoes = new Violacoes();
        regra.validar(new ItemProposto(1L, 1), violacoes);
        assertThat(violacoes.lista()).isEmpty();
    }

    @Test
    void rfVen002_quantidadeZeroOuNegativaEhRecusada() {
        Violacoes violacoes = new Violacoes();
        regra.validar(new ItemProposto(1L, 0), violacoes);
        regra.validar(new ItemProposto(1L, -2), violacoes);
        assertThat(violacoes.lista()).hasSize(2).allMatch(violacao -> violacao.codigo().equals("RF-VEN-002"));
    }
}
