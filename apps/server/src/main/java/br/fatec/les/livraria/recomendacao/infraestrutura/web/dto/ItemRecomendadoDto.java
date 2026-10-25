package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroListadoDto;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;

/**
 * Item de "Recomendados para você" (RNF-REC-001, DEC-03, DEC-07). O livro tem o mesmo formato da vitrine;
 * {@code justificativa} e {@code avaliacao} são omitidas quando não existem.
 */
public record ItemRecomendadoDto(
        Long id,
        LivroListadoDto livro,
        @Nullable String justificativa,
        @Nullable Avaliacao avaliacao) {
}
