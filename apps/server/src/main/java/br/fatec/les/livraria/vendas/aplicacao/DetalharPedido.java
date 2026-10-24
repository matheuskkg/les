package br.fatec.les.livraria.vendas.aplicacao;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.troca.PrazoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;
import lombok.RequiredArgsConstructor;

/** Junta ao pedido as trocas e o último dia para pedir troca, comum aos detalhes do admin e do cliente (DEC-12). */
@Component
@RequiredArgsConstructor
public class DetalharPedido {

    private final TrocaRepositorio trocas;
    private final PropriedadesDaTroca propriedades;

    public PedidoDetalhado executar(Pedido pedido) {
        return new PedidoDetalhado(new TrocasDoPedido(pedido, trocas.listarDoPedido(pedido.getId())),
                pedido.getEntregueEm() == null ? null
                        : PrazoDeTroca.ultimoDia(pedido.getEntregueEm(), propriedades.prazo()));
    }
}
