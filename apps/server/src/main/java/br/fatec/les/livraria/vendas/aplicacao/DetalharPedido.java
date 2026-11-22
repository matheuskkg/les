package br.fatec.les.livraria.vendas.aplicacao;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.PrazoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Junta ao pedido as trocas, o último dia para pedir troca e se ela ainda pode ser pedida, comum aos detalhes do admin
 * e do cliente (RF-VEN-011, RN-VEN-013, DEC-12). Só pedido entregue tem trocas, então os outros não as buscam.
 */
@Component
@RequiredArgsConstructor
public class DetalharPedido {

    private final TrocaRepositorio trocas;
    private final PropriedadesDaTroca propriedades;
    private final Relogio relogio;

    public PedidoDetalhado executar(Pedido pedido) {
        LocalDateTime entregueEm = pedido.getEntregueEm();
        if (pedido.getStatus() != StatusPedido.ENTREGUE || entregueEm == null) {
            return new PedidoDetalhado(new TrocasDoPedido(pedido, List.of()), null, false);
        }
        TrocasDoPedido doPedido = new TrocasDoPedido(pedido, trocas.listarDoPedido(pedido.getId()));
        boolean podeSolicitar =
                PrazoDeTroca.dentro(entregueEm, propriedades.prazo(), relogio.agora())
                        && pedido.getItens().stream().anyMatch(item -> doPedido.quantidadeDisponivel(item) > 0);
        return new PedidoDetalhado(doPedido, PrazoDeTroca.ultimoDia(entregueEm, propriedades.prazo()), podeSolicitar);
    }
}
