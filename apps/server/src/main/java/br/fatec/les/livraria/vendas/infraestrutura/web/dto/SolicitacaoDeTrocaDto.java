package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca.ItemSolicitado;

/**
 * Pedido de troca do cliente (RF-VEN-011, DEC-12): os itens do pedido e quantas unidades de cada um. Item ou
 * quantidade ausentes chegam às regras, que respondem 422.
 */
public record SolicitacaoDeTrocaDto(@Nullable List<ItemSolicitadoDto> itens) {

    public List<ItemSolicitado> paraItens() {
        return itens == null ? List.of() : itens.stream()
                .filter(Objects::nonNull)
                .map(item -> new ItemSolicitado(item.itemPedidoId(),
                        item.quantidade() == null ? 0 : item.quantidade()))
                .toList();
    }

    /**
     * @param itemPedidoId item do pedido
     * @param quantidade   unidades a trocar
     */
    public record ItemSolicitadoDto(@Nullable Long itemPedidoId, @Nullable Integer quantidade) {
    }
}
