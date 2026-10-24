package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Item do carrinho para a API (RF-VEN-001, DEC-07). O preço unitário é o preço de venda atual do catálogo,
 * não um preço congelado no carrinho (DEC-12, DEC-16).
 *
 * @param livroId       código do livro
 * @param titulo        título atual do livro
 * @param autores       autores atuais do livro
 * @param precoUnitario preço de venda atual
 * @param quantidade    unidades no carrinho
 * @param subtotal      preço unitário × quantidade
 */
public record ItemDoCarrinho(
        Long livroId,
        String titulo,
        List<String> autores,
        Dinheiro precoUnitario,
        int quantidade,
        Dinheiro subtotal) {

    public ItemDoCarrinho {
        Objects.requireNonNull(precoUnitario, "precoUnitario");
        Objects.requireNonNull(subtotal, "subtotal");
        autores = List.copyOf(autores);
    }
}
