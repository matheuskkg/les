package br.fatec.les.livraria.recomendacao.dominio.modelo;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Livro de uma recomendação da vitrine, na posição em que a IA o colocou (RNF-REC-001, DEC-03). A justificativa é vazia
 * quando a recomendação foi gravada sem IA, na ordem da pontuação.
 *
 * @param id id do item, nulo antes de gravar
 * @param livroId livro recomendado
 * @param posicao posição na vitrine, a partir de 1
 * @param justificativa texto curto da IA, ou nulo
 * @param avaliacao última avaliação do cliente, ou nula
 * @param avaliadaEm quando a última avaliação foi dada, ou nulo
 */
public record ItemDaRecomendacao(
        @Nullable Long id,
        Long livroId,
        int posicao,
        @Nullable String justificativa,
        @Nullable Avaliacao avaliacao,
        @Nullable LocalDateTime avaliadaEm) {

    public ItemDaRecomendacao {
        Objects.requireNonNull(livroId, "livroId");
    }

    /** Item novo, ainda sem avaliação. */
    public static ItemDaRecomendacao novo(Long livroId, int posicao, @Nullable String justificativa) {
        return new ItemDaRecomendacao(null, livroId, posicao, justificativa, null, null);
    }

    /** Troca a avaliação pela nova; a última vale (DEC-03). */
    public ItemDaRecomendacao avaliar(Avaliacao nova, LocalDateTime em) {
        return new ItemDaRecomendacao(
                id,
                livroId,
                posicao,
                justificativa,
                Objects.requireNonNull(nova, "nova"),
                Objects.requireNonNull(em, "em"));
    }

    public boolean avaliadoNegativamente() {
        return avaliacao == Avaliacao.NEGATIVA;
    }
}
