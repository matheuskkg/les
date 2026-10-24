package br.fatec.les.livraria.vendas.dominio.regra;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca.ItemSolicitado;

/**
 * A troca precisa de ao menos um item, cada um informado uma única vez, e todos do próprio pedido (RF-VEN-011,
 * DEC-12).
 */
@Component
public class ItensDaTrocaInformados implements Regra<SolicitacaoDeTroca> {

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        if (alvo.itens().isEmpty()) {
            violacoes.adicionar("RF-VEN-011", "Escolha ao menos um item para trocar.");
            return;
        }
        Set<Long> doPedido = new HashSet<>();
        alvo.pedido().getItens().forEach(item -> doPedido.add(item.id()));
        Set<Long> vistos = new HashSet<>();
        for (ItemSolicitado item : alvo.itens()) {
            if (item.itemPedidoId() == null || !doPedido.contains(item.itemPedidoId())) {
                violacoes.adicionar("RF-VEN-011", "O item informado não pertence a este pedido.");
            } else if (!vistos.add(item.itemPedidoId())) {
                violacoes.adicionar("RF-VEN-011", "Cada item só pode aparecer uma vez na troca.");
            }
        }
    }
}
