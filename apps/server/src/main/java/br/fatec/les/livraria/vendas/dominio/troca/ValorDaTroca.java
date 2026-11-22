package br.fatec.les.livraria.vendas.dominio.troca;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;

/**
 * Valor do cupom gerado no recebimento da troca (RF-VEN-015, DEC-11, DEC-12): a soma do preço unitário de venda de cada
 * item × a quantidade trocada, sem frete e sem ratear o desconto do promocional. Vale também para os itens que não
 * voltam ao estoque.
 */
public final class ValorDaTroca {

    private ValorDaTroca() {}

    /**
     * @throws IllegalArgumentException se um item da troca não for do pedido
     */
    public static Dinheiro de(Troca troca, Pedido pedido) {
        Dinheiro total = Dinheiro.ZERO;
        for (ItemTroca item : troca.getItens()) {
            ItemPedido doPedido = pedido.item(item.itemPedidoId());
            total = total.somar(doPedido.precoUnitario().multiplicar(item.quantidade()));
        }
        return total;
    }
}
