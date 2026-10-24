package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.Objects;

/**
 * Cópia do cupom usado no pedido (DEC-12, DEC-16), com o id de origem na tabela da sua categoria.
 *
 * @param tipo tipo do promocional ({@code VALOR_FIXO}, {@code PERCENTUAL}...); nulo para o cupom de troca
 */
public record CupomNoPedido(Long id, String codigo, CategoriaCupom categoria, String tipo) {

    public CupomNoPedido {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(categoria, "categoria");
    }
}
