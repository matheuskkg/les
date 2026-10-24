package br.fatec.les.livraria.vendas.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;

/** Só itens de pedidos {@code ENTREGUE} podem ser trocados (RN-VEN-013). */
@Component
public class PedidoEntregueParaTroca implements Regra<SolicitacaoDeTroca> {

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        if (alvo.pedido().getStatus() != StatusPedido.ENTREGUE) {
            violacoes.adicionar("RN-VEN-013", "Só é possível trocar itens de um pedido entregue.");
        }
    }
}
