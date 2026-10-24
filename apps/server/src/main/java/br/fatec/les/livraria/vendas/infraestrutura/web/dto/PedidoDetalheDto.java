package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.CupomPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.EnderecoNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.PagamentoCartao;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/**
 * Detalhe do pedido com as cópias gravadas na finalização (RF-VEN-009, RF-VEN-010, DEC-12): itens, cliente,
 * endereços, cartões, cupons e valores. Datas em UTC; {@code entregueEm} é nulo até a entrega.
 */
public record PedidoDetalheDto(
        Long id,
        StatusPedido status,
        Instant realizadoEm,
        @Nullable Instant entregueEm,
        ClienteNoPedido cliente,
        EnderecoNoPedido enderecoEntrega,
        EnderecoNoPedido enderecoCobranca,
        List<ItemDoPedidoDto> itens,
        List<PagamentoCartao> cartoes,
        List<CupomPedido> cupons,
        Dinheiro subtotal,
        Dinheiro frete,
        Dinheiro descontoPromocional,
        Dinheiro total) {

    public static PedidoDetalheDto de(Pedido pedido) {
        return new PedidoDetalheDto(pedido.getId(), pedido.getStatus(), emUtc(pedido.getRealizadoEm()),
                emUtc(pedido.getEntregueEm()), pedido.getCliente(), pedido.getEnderecoEntrega(),
                pedido.getEnderecoCobranca(), pedido.getItens().stream().map(ItemDoPedidoDto::de).toList(),
                pedido.getCartoes(), pedido.getCupons(), pedido.getSubtotal(), pedido.getFrete(),
                pedido.getDescontoPromocional(), pedido.getTotal());
    }

    private static Instant emUtc(LocalDateTime data) {
        return data == null ? null : data.atZone(RelogioDoSistema.FUSO).toInstant();
    }
}
