package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.LivroNoPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.dominio.troca.TrocasDoPedido;

/**
 * Item copiado no pedido, com o subtotal de preço unitário × quantidade (DEC-12), a quantidade ainda disponível para
 * troca e o status da troca mais recente que o inclui, ausente se ele nunca foi trocado.
 */
public record ItemDoPedidoDto(Long id, LivroNoPedido livro, Dinheiro precoUnitario, int quantidade,
        Dinheiro subtotal, int disponivelParaTroca, @Nullable StatusTroca statusDaTroca) {

    public static ItemDoPedidoDto de(ItemPedido item, TrocasDoPedido trocas) {
        return new ItemDoPedidoDto(item.id(), item.livro(), item.precoUnitario(), item.quantidade(),
                item.subtotal(), trocas.quantidadeDisponivel(item), trocas.statusDoItem(item).orElse(null));
    }
}
