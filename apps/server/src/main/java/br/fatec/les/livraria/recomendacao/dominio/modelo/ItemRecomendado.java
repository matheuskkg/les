package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Livro indicado pelo assistente, com a justificativa curta que a vitrine exibe (RNF-REC-001, DEC-03). O
 * {@code livroId} é o mesmo id do módulo {@code catalogo}.
 */
public record ItemRecomendado(
        Long livroId,
        String justificativa) {
}
