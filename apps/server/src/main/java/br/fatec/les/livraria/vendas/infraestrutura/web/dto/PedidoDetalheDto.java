package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.CupomPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.EnderecoNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.PagamentoCartao;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.IndicadorDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;

/**
 * Detalhe do pedido com as cópias gravadas na finalização (RF-VEN-009, RF-VEN-010, DEC-12): itens, cliente,
 * endereços, cartões, cupons e valores. Datas em UTC; {@code entregueEm} é nulo até a entrega. Com as trocas
 * (RF-VEN-011): o indicador {@code troca}, ausente sem troca, o último dia para pedir troca, ausente antes da entrega,
 * se o cliente ainda pode pedir troca agora ({@code podeSolicitarTroca}) e as trocas da mais recente para a mais
 * antiga.
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
        Dinheiro total,
        @Nullable IndicadorDeTroca troca,
        @Nullable LocalDate trocaAte,
        boolean podeSolicitarTroca,
        List<TrocaDto> trocas) {

    public static PedidoDetalheDto de(PedidoDetalhado detalhado) {
        TrocasDoPedido trocas = detalhado.trocas();
        Pedido pedido = trocas.pedido();
        return new PedidoDetalheDto(pedido.getId(), pedido.getStatus(), emUtc(pedido.getRealizadoEm()),
                emUtc(pedido.getEntregueEm()), pedido.getCliente(), pedido.getEnderecoEntrega(),
                pedido.getEnderecoCobranca(),
                pedido.getItens().stream().map(item -> ItemDoPedidoDto.de(item, trocas)).toList(),
                pedido.getCartoes(), pedido.getCupons(), pedido.getSubtotal(), pedido.getFrete(),
                pedido.getDescontoPromocional(), pedido.getTotal(), trocas.indicador().orElse(null),
                detalhado.trocaAte(), detalhado.podeSolicitarTroca(), trocas.trocas().stream().map(TrocaDto::de).toList());
    }

    static Instant emUtc(LocalDateTime data) {
        return data == null ? null : data.atZone(RelogioDoSistema.FUSO).toInstant();
    }
}
