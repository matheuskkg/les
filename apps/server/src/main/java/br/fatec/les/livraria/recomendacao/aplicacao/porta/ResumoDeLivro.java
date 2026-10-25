package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Livro como as tools de busca o devolvem ao assistente (RNF-REC-001, DEC-03). Só livros ativos e com estoque
 * chegam aqui.
 */
public record ResumoDeLivro(
        Long id,
        String titulo,
        List<String> autores,
        List<String> categorias,
        Dinheiro preco) {

    public ResumoDeLivro {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
