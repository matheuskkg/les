package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Avaliação que o cliente deu a um livro recomendado ou a uma resposta do assistente (RNF-REC-001, DEC-03). A
 * avaliação entra no perfil e no cálculo dos candidatos da vitrine.
 */
public record AvaliacaoDada(
        Long livroId,
        boolean positiva) {
}
