package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Item do carrinho para a API (RF-VEN-001, DEC-07). O preço unitário é o preço de venda atual do catálogo,
 * não um preço congelado no carrinho (DEC-12, DEC-16).
 *
 * @param livroId       código do livro
 * @param titulo        título atual do livro; nulo se o livro não existe mais
 * @param autores       autores atuais do livro
 * @param precoUnitario preço de venda atual; nulo se o livro saiu de venda
 * @param quantidade    unidades no carrinho
 * @param subtotal      preço unitário × quantidade
 */
public record ItemDoCarrinho(
        Long livroId,
        @Nullable String titulo,
        List<String> autores,
        @Nullable Dinheiro precoUnitario,
        int quantidade,
        @Nullable Dinheiro subtotal) {

    public ItemDoCarrinho {
        autores = List.copyOf(autores);
    }
}
