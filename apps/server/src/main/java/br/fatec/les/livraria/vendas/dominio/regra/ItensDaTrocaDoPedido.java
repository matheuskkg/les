package br.fatec.les.livraria.vendas.dominio.regra;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;

/** Cada item da troca é um item do próprio pedido (RF-VEN-011, DEC-12). */
@Component
public class ItensDaTrocaDoPedido implements Regra<SolicitacaoDeTroca> {

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        Set<Long> doPedido = alvo.pedido().getItens().stream().map(ItemPedido::id).collect(Collectors.toSet());
        boolean algumDeFora = alvo.itens().stream()
                .anyMatch(item -> item.itemPedidoId() == null || !doPedido.contains(item.itemPedidoId()));
        if (algumDeFora) {
            violacoes.adicionar("RF-VEN-011", "O item informado não pertence a este pedido.");
        }
    }
}
