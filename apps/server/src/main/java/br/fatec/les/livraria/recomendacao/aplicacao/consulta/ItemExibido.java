package br.fatec.les.livraria.recomendacao.aplicacao.consulta;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;

/**
 * Item de "Recomendados para você" pronto para a vitrine (RNF-REC-001, DEC-03): o livro com preço e disponível, a
 * justificativa da IA e a avaliação do cliente.
 *
 * @param itemId        id do item, usado na avaliação
 * @param livro         livro ativo com estoque disponível
 * @param justificativa texto curto da IA, ou nulo na recomendação sem IA
 * @param avaliacao     última avaliação do cliente, ou nula
 */
public record ItemExibido(Long itemId, LivroListado livro, @Nullable String justificativa,
        @Nullable Avaliacao avaliacao) {

    public ItemExibido {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(livro, "livro");
    }
}
