package br.fatec.les.livraria.recomendacao.aplicacao.porta;

/**
 * Detalhe de um livro consultado pela tool {@code verLivro} (RNF-REC-001, DEC-03), com a sinopse e o disponível em
 * estoque.
 */
public record DetalheDoLivro(
        ResumoDeLivro livro,
        String sinopse,
        int disponivel) {
}
