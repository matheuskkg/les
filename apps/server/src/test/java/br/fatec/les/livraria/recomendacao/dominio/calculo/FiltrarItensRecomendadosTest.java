package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemRecomendado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class FiltrarItensRecomendadosTest {

    private final FiltrarItensRecomendados filtrar = new FiltrarItensRecomendados();
    private final List<LivroCandidato> candidatos = List.of(candidato(1L), candidato(2L));

    private static LivroCandidato candidato(Long id) {
        return new LivroCandidato(id, "Livro", List.of(), List.of(), Dinheiro.de("1.00"), BigDecimal.ONE);
    }

    @Test
    void rnfRec001_soValemIdsDosCandidatosSemRepetirNaOrdemDaIa() {
        List<ItemDaRecomendacao> itens =
                filtrar.filtrar(
                        List.of(
                                new ItemRecomendado(2L, "Bom"),
                                new ItemRecomendado(99L, "Inventado"),
                                new ItemRecomendado(2L, "Repetido"),
                                new ItemRecomendado(null, "Sem id"),
                                new ItemRecomendado(1L, "  ")),
                        candidatos);

        assertThat(itens).extracting(ItemDaRecomendacao::livroId).containsExactly(2L, 1L);
        assertThat(itens).extracting(ItemDaRecomendacao::posicao).containsExactly(1, 2);
        assertThat(itens).extracting(ItemDaRecomendacao::justificativa).containsExactly("Bom", null);
    }

    @Test
    void rnfRec001_justificativaLongaECortadaNoTamanhoDaColuna() {
        List<ItemDaRecomendacao> itens = filtrar.filtrar(List.of(new ItemRecomendado(1L, "a".repeat(400))), candidatos);

        assertThat(itens.getFirst().justificativa()).hasSize(FiltrarItensRecomendados.TAMANHO_DA_JUSTIFICATIVA);
    }

    @Test
    void rnfRec001_justificativaLongaCortadaSemPartirEmoji() {
        String longa = "a".repeat(FiltrarItensRecomendados.TAMANHO_DA_JUSTIFICATIVA - 1) + "📚fim";

        String cortada =
                filtrar.filtrar(List.of(new ItemRecomendado(1L, longa)), candidatos).getFirst().justificativa();

        assertThat(cortada)
                .endsWith("📚")
                .satisfies(texto ->
                        assertThat(texto.codePointCount(0, texto.length()))
                                .isEqualTo(FiltrarItensRecomendados.TAMANHO_DA_JUSTIFICATIVA));
    }
}
