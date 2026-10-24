package br.fatec.les.livraria.compartilhado.aplicacao.porta;

/**
 * Autor de uma escrita resolvido em uma única leitura do contexto (RNF-GER-002, DEC-08).
 *
 * <p>{@code usuarioId} é nulo sem login ou fora de requisição HTTP; {@code usuario} nunca é nulo.</p>
 */
public record Autor(Long usuarioId, String usuario) {
}
