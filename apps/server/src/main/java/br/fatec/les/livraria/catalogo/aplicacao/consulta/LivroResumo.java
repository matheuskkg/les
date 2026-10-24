package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.util.List;

/**
 * Livro como a consulta o devolve, sem preço nem disponibilidade, que são enriquecidos pelos casos de uso
 * (RF-LIV-005, DEC-15.3). Só livros ativos chegam aqui.
 */
public record LivroResumo(
        Long id,
        String titulo,
        List<String> autores,
        List<CategoriaDoLivro> categorias,
        String editora,
        Integer ano) {

    public LivroResumo {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
