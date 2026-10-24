package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Detalhe do livro com o preço de venda e o disponível já calculados (RF-LIV-005, DEC-15.3). Livro sem
 * preço aparece sem {@code precoVenda} e com disponível zero.
 */
public record LivroConsultado(
        Long id,
        String titulo,
        List<String> autores,
        List<CategoriaDoLivro> categorias,
        String editora,
        Integer ano,
        @Nullable Dinheiro precoVenda,
        int disponivel,
        String edicao,
        String isbn,
        Integer paginas,
        String sinopse,
        BigDecimal altura,
        BigDecimal largura,
        BigDecimal profundidade,
        BigDecimal peso,
        String codigoBarras) {

    public LivroConsultado {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
