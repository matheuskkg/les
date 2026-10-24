package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Detalhe do livro com o preço de venda e o disponível já calculados (RF-LIV-005, DEC-15.3). Livro sem
 * preço aparece sem {@code precoVenda} e com disponível zero.
 */
public record LivroConsultado(
        LivroDetalhe livro,
        @Nullable Dinheiro precoVenda,
        int disponivel) {

    public LivroConsultado {
        Objects.requireNonNull(livro, "livro");
    }
}
