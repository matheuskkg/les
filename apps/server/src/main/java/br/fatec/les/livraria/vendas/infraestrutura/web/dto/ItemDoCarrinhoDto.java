package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemDoCarrinho;

/** Item do carrinho na API (RF-VEN-001, DEC-07): preço de venda atual e subtotal em string decimal. */
public record ItemDoCarrinhoDto(
        Long livroId,
        String titulo,
        List<String> autores,
        Dinheiro precoUnitario,
        int quantidade,
        Dinheiro subtotal) {

    public static ItemDoCarrinhoDto de(ItemDoCarrinho item) {
        return new ItemDoCarrinhoDto(item.livroId(), item.titulo(), item.autores(), item.precoUnitario(),
                item.quantidade(), item.subtotal());
    }
}
