package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.LivroNoPedido;

/** Item copiado no pedido, com o subtotal de preço unitário × quantidade (DEC-12). */
public record ItemDoPedidoDto(Long id, LivroNoPedido livro, Dinheiro precoUnitario, int quantidade,
        Dinheiro subtotal) {

    public static ItemDoPedidoDto de(ItemPedido item) {
        return new ItemDoPedidoDto(item.id(), item.livro(), item.precoUnitario(), item.quantidade(),
                item.subtotal());
    }
}
