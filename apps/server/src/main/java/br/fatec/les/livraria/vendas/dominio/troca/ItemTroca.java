package br.fatec.les.livraria.vendas.dominio.troca;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Item do pedido incluído numa troca, com a quantidade trocada (DEC-12).
 *
 * @param id           nulo antes de gravar
 * @param itemPedidoId item do pedido trocado
 * @param quantidade   unidades trocadas, ao menos uma
 * @param voltaEstoque se o item volta ao estoque, informado no recebimento (RF-VEN-014); nulo até lá
 */
public record ItemTroca(@Nullable Long id, Long itemPedidoId, int quantidade, @Nullable Boolean voltaEstoque) {

    public ItemTroca {
        Objects.requireNonNull(itemPedidoId, "itemPedidoId");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade trocada deve ser positiva.");
        }
    }
}
