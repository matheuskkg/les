package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemDoCarrinho;

/**
 * Item do carrinho na API (RF-VEN-001, DEC-07): preço de venda atual e subtotal em string decimal, ausentes quando o
 * livro saiu de venda.
 */
public record ItemDoCarrinhoDto(
        Long livroId,
        @Nullable String titulo,
        List<String> autores,
        @Nullable Dinheiro precoUnitario,
        int quantidade,
        @Nullable Dinheiro subtotal) {

    public static ItemDoCarrinhoDto de(ItemDoCarrinho item) {
        return new ItemDoCarrinhoDto(item.livroId(), item.titulo(), item.autores(), item.precoUnitario(),
                item.quantidade(), item.subtotal());
    }
}
