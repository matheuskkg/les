package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.List;
import java.util.Objects;

/** Cópia do livro vendido no item do pedido (DEC-12, DEC-16), com o id de origem. */
public record LivroNoPedido(
        Long id,
        String titulo,
        List<String> autores,
        String editora,
        String edicao,
        String isbn,
        List<CategoriaNoPedido> categorias) {

    public LivroNoPedido {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(titulo, "titulo");
        autores = autores == null ? List.of() : List.copyOf(autores);
        categorias = categorias == null ? List.of() : List.copyOf(categorias);
    }
}
