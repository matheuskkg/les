package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;

/**
 * Troca de um pedido (RF-VEN-011, DEC-12). O motivo só existe na recusa e o valor do cupom só no recebimento;
 * {@code solicitadaEm} em UTC.
 */
public record TrocaDto(Long id, Long pedidoId, StatusTroca status, Instant solicitadaEm,
        @Nullable String motivoRecusa, @Nullable Dinheiro valorCupom, List<ItemDaTrocaDto> itens) {

    public static TrocaDto de(Troca troca) {
        return new TrocaDto(troca.getId(), troca.getPedidoId(), troca.getStatus(),
                PedidoDetalheDto.emUtc(troca.getSolicitadaEm()), troca.getMotivoRecusa(), troca.getValorCupom(),
                troca.getItens().stream().map(ItemDaTrocaDto::de).toList());
    }

    /**
     * Item do pedido incluído na troca.
     *
     * @param voltaEstoque informado no recebimento; ausente até lá
     */
    public record ItemDaTrocaDto(Long itemPedidoId, int quantidade, @Nullable Boolean voltaEstoque) {

        static ItemDaTrocaDto de(ItemTroca item) {
            return new ItemDaTrocaDto(item.itemPedidoId(), item.quantidade(), item.voltaEstoque());
        }
    }
}
