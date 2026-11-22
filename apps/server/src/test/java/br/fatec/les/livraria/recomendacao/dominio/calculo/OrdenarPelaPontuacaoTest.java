package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class OrdenarPelaPontuacaoTest {

    @Test
    void rnfRec001_semIaUsaAOrdemDaPontuacaoSemJustificativa() {
        List<LivroCandidato> candidatos =
                List.of(
                        new LivroCandidato(1L, "Um", List.of(), List.of(), Dinheiro.de("10.00"), BigDecimal.TWO),
                        new LivroCandidato(2L, "Dois", List.of(), List.of(), Dinheiro.de("10.00"), BigDecimal.ONE));

        assertThat(new OrdenarPelaPontuacao().ordenar(candidatos))
                .extracting(ItemDaRecomendacao::livroId, ItemDaRecomendacao::posicao, ItemDaRecomendacao::justificativa)
                .containsExactly(tuple(1L, 1, null), tuple(2L, 2, null));
    }
}
