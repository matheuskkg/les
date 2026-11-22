package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.vendas.dominio.troca.RecebimentoDeTroca.ItemRecebido;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Recebimento dos itens de uma troca pelo admin (RF-VEN-014, DEC-12): se cada item volta ao estoque. Item ou decisão
 * ausentes chegam à regra, que responde 422.
 */
public record RecebimentoDeTrocaDto(@Nullable List<ItemRecebidoDto> itens) {

    public List<ItemRecebido> paraItens() {
        return itens == null
                ? List.of()
                : itens.stream()
                        .filter(Objects::nonNull)
                        .map(item -> new ItemRecebido(item.itemPedidoId(), item.voltaEstoque()))
                        .toList();
    }

    /**
     * @param itemPedidoId item do pedido que está na troca
     * @param voltaEstoque se o item volta a ser vendável
     */
    public record ItemRecebidoDto(@Nullable Long itemPedidoId, @Nullable Boolean voltaEstoque) {}
}
