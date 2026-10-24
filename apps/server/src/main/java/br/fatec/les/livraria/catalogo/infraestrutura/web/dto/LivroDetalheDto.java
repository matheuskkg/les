package br.fatec.les.livraria.catalogo.infraestrutura.web.dto;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Detalhe de um livro da vitrine (RF-LIV-005, RN-LIV-001, DEC-07). O {@code id} é o código do livro
 * (RNF-LIV-001). {@code precoVenda} sai como string decimal e é omitido quando o livro não tem preço.
 */
public record LivroDetalheDto(
        Long id,
        String titulo,
        List<String> autores,
        List<CategoriaDto> categorias,
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
}
