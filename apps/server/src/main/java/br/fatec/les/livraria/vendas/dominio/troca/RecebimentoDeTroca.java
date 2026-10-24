package br.fatec.les.livraria.vendas.dominio.troca;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * O que o admin informa ao confirmar o recebimento da troca: se cada item volta ao estoque (RF-VEN-014, DEC-12). Alvo
 * das regras do recebimento.
 *
 * @param troca a troca recebida
 * @param itens decisão por item do pedido, como chegou
 */
public record RecebimentoDeTroca(Troca troca, List<ItemRecebido> itens) {

    public RecebimentoDeTroca {
        Objects.requireNonNull(troca, "troca");
        itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
    }

    /**
     * @param itemPedidoId item do pedido que está na troca
     * @param voltaEstoque se o item volta a ser vendável; nulo quando não informado
     */
    public record ItemRecebido(@Nullable Long itemPedidoId, @Nullable Boolean voltaEstoque) {
    }
}
