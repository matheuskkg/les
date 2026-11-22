package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemComPreco;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Item do carrinho ou do checkout na API (RF-VEN-001, RF-VEN-003, DEC-07): preço de venda atual e subtotal em string
 * decimal, ausentes quando o livro saiu de venda.
 */
public record ItemComPrecoDto(
        Long livroId,
        @Nullable String titulo,
        List<String> autores,
        @Nullable Dinheiro precoUnitario,
        int quantidade,
        @Nullable Dinheiro subtotal) {

    public static ItemComPrecoDto de(ItemComPreco item) {
        return new ItemComPrecoDto(
                item.livroId(),
                item.titulo(),
                item.autores(),
                item.precoUnitario(),
                item.quantidade(),
                item.subtotal());
    }
}
