package br.fatec.les.livraria.catalogo.aplicacao.consulta;

/**
 * Sinopse do livro, usada pela busca do assistente do {@code recomendacao} (RNF-REC-001, DEC-03).
 *
 * @param id      id do livro
 * @param sinopse texto da sinopse
 */
public record SinopseDoLivro(
        Long id,
        String sinopse) {
}
