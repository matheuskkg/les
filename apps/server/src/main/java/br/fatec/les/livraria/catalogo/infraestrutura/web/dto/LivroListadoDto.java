package br.fatec.les.livraria.catalogo.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Item da listagem da vitrine (RF-LIV-005, DEC-07). O {@code id} é o código do livro (RNF-LIV-001). {@code precoVenda}
 * sai como string decimal e é omitido quando o livro não tem preço.
 */
public record LivroListadoDto(
        Long id,
        String titulo,
        List<String> autores,
        List<CategoriaDto> categorias,
        String editora,
        Integer ano,
        @Nullable Dinheiro precoVenda,
        int disponivel) {}
