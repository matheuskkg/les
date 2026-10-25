package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;

class PontuarCandidatosTest {

    private final PontuarCandidatos pontuar = new PontuarCandidatos();

    private static LivroCandidato livro(long id, String categoria, String autor) {
        return new LivroCandidato(id, "Livro " + id, List.of(autor), List.of(categoria), Dinheiro.de("10.00"),
                BigDecimal.ZERO);
    }

    @Test
    void rnfRec001_somaCategoriasAutoresECoCompra() {
        List<LivroComprado> referencias = List.of(
                new LivroComprado(1L, "Lido", List.of("Romance"), List.of("Machado")));
        List<LivroCandidato> disponiveis = List.of(
                livro(2, "Romance", "Machado"),
                livro(3, "Romance", "Outro"),
                livro(4, "Terror", "Outro"));

        List<LivroCandidato> candidatos = pontuar.pontuar(disponiveis, referencias, Set.of(), Map.of(4L, 5),
                Map.of());

        assertThat(candidatos).extracting(LivroCandidato::id).containsExactly(4L, 2L, 3L);
        assertThat(candidatos).extracting(LivroCandidato::pontuacao)
                .containsExactly(BigDecimal.valueOf(5), BigDecimal.valueOf(2), BigDecimal.valueOf(1));
    }

    @Test
    void rnfRec001_avaliadoPositivamenteNaoPontuaContraSiMesmo() {
        // O livro 2 foi avaliado positivamente e é referência; ele só pontua pelo que tem em comum com o 1.
        List<LivroComprado> referencias = List.of(
                new LivroComprado(1L, "Lido", List.of("Romance"), List.of("Machado")),
                new LivroComprado(2L, "Curtido", List.of("Terror"), List.of("King")));

        List<LivroCandidato> candidatos = pontuar.pontuar(
                List.of(livro(2, "Terror", "King"), livro(3, "Terror", "King")),
                referencias, Set.of(), Map.of(), Map.of());

        assertThat(candidatos).extracting(LivroCandidato::id, LivroCandidato::pontuacao)
                .containsExactly(tuple(3L, BigDecimal.valueOf(2)),
                        tuple(2L, BigDecimal.ZERO));
    }

    @Test
    void rnfRec001_excluiCompradosEAvaliadosNegativamente() {
        List<LivroCandidato> candidatos = pontuar.pontuar(
                List.of(livro(1, "A", "X"), livro(2, "A", "X"), livro(3, "A", "X")),
                List.of(new LivroComprado(1L, "Lido", List.of("A"), List.of("X"))),
                Set.of(1L, 2L), Map.of(), Map.of());

        assertThat(candidatos).extracting(LivroCandidato::id).containsExactly(3L);
    }

    @Test
    void rnfRec001_semHistoricoOrdenaPelosMaisVendidos() {
        List<LivroCandidato> candidatos = pontuar.pontuar(
                List.of(livro(1, "A", "X"), livro(2, "B", "Y"), livro(3, "C", "Z")),
                List.of(), Set.of(), Map.of(), Map.of(2L, 10, 3L, 4));

        assertThat(candidatos).extracting(LivroCandidato::id).containsExactly(2L, 3L, 1L);
    }

    @Test
    void rnfRec001_limitaAOitoCandidatos() {
        List<LivroCandidato> disponiveis = LongStream.rangeClosed(1, 12).mapToObj(id -> livro(id, "A", "X"))
                .toList();

        assertThat(pontuar.pontuar(disponiveis, List.of(), Set.of(), Map.of(), Map.of()))
                .hasSize(PontuarCandidatos.LIMITE)
                .extracting(LivroCandidato::id).startsWith(1L, 2L);
    }
}
