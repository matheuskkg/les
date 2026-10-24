package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Livro completo como a consulta o devolve, sem preço nem disponibilidade, que são enriquecidos pelo caso
 * de uso (RF-LIV-005, RN-LIV-001). Só livros ativos chegam aqui.
 */
public record LivroDetalhe(
        Long id,
        String titulo,
        List<String> autores,
        List<CategoriaDoLivro> categorias,
        String editora,
        Integer ano,
        String edicao,
        String isbn,
        Integer paginas,
        String sinopse,
        BigDecimal altura,
        BigDecimal largura,
        BigDecimal profundidade,
        BigDecimal peso,
        String codigoBarras) {

    public LivroDetalhe {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
