package br.fatec.les.livraria.vendas.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca.ItemRecebido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class RetornoAoEstoqueDeCadaItemTest {

    private static final Troca TROCA =
            new Troca(
                    1L,
                    1L,
                    StatusTroca.TROCA_AUTORIZADA,
                    LocalDateTime.of(2026, 10, 24, 12, 0),
                    List.of(new ItemTroca(1L, 101L, 2, null), new ItemTroca(2L, 102L, 1, null)),
                    null,
                    null,
                    null);

    @Test
    void rfVen014_cadaItemDaTrocaTemADecisaoDoRetorno() {
        assertThat(violacoes(new ItemRecebido(101L, true), new ItemRecebido(102L, false))).isEmpty();
    }

    @Test
    void rfVen014_faltaRepeticaoOuItemDeForaViolam() {
        assertThat(violacoes(new ItemRecebido(101L, true))).extracting(Violacao::codigo).containsExactly("RF-VEN-014");
        assertThat(violacoes(new ItemRecebido(101L, true), new ItemRecebido(102L, null))).hasSize(1);
        assertThat(violacoes(new ItemRecebido(101L, true), new ItemRecebido(101L, false), new ItemRecebido(102L, true)))
                .hasSize(1);
        assertThat(violacoes(new ItemRecebido(101L, true), new ItemRecebido(102L, true), new ItemRecebido(999L, true)))
                .hasSize(1);
        assertThat(violacoes()).hasSize(1);
    }

    private static List<Violacao> violacoes(ItemRecebido... itens) {
        Violacoes violacoes = new Violacoes();
        new RetornoAoEstoqueDeCadaItem().validar(new RecebimentoDeTroca(TROCA, List.of(itens)), violacoes);
        return violacoes.lista();
    }
}
