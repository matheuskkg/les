package br.fatec.les.livraria.vendas.dominio.troca;

import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;

/**
 * O que as regras da solicitação de troca validam (RF-VEN-011, RN-VEN-013, DEC-12).
 *
 * @param trocas o pedido com as trocas já gravadas
 * @param itens itens e quantidades pedidos pelo cliente, como vieram
 * @param prazo prazo de troca contado da entrega ({@code livraria.troca.prazo})
 * @param agora instante da solicitação
 */
public record SolicitacaoDeTroca(TrocasDoPedido trocas, List<ItemSolicitado> itens, Period prazo, LocalDateTime agora) {

    public SolicitacaoDeTroca {
        Objects.requireNonNull(trocas, "trocas");
        itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
        Objects.requireNonNull(prazo, "prazo");
        Objects.requireNonNull(agora, "agora");
    }

    public Pedido pedido() {
        return trocas.pedido();
    }

    /**
     * Item pedido para troca, como veio da API.
     *
     * @param itemPedidoId item do pedido; nulo quando não informado
     * @param quantidade unidades pedidas
     */
    public record ItemSolicitado(@Nullable Long itemPedidoId, int quantidade) {}
}
