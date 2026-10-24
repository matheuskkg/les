package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Item da vitrine: o resumo do livro com o preço de venda e o disponível já calculados (RF-LIV-005,
 * DEC-15.3). Livro sem preço aparece sem {@code precoVenda} e com disponível zero.
 */
public record LivroListado(
        Long id,
        String titulo,
        List<String> autores,
        List<CategoriaDoLivro> categorias,
        String editora,
        Integer ano,
        @Nullable Dinheiro precoVenda,
        int disponivel) {

    public LivroListado {
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
