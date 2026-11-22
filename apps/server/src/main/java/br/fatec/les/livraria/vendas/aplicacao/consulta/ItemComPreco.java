package br.fatec.les.livraria.vendas.aplicacao.consulta;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Item do carrinho ou do checkout para a API (RF-VEN-001, RF-VEN-003, DEC-07). O preço unitário é o preço de venda
 * atual do catálogo, não um preço congelado: só o pedido copia o preço (DEC-12, DEC-16).
 *
 * @param livroId código do livro
 * @param titulo título atual do livro; nulo se o livro não existe mais
 * @param autores autores atuais do livro
 * @param precoUnitario preço de venda atual; nulo se o livro saiu de venda
 * @param quantidade unidades do item
 * @param subtotal preço unitário × quantidade
 */
public record ItemComPreco(
        Long livroId,
        @Nullable String titulo,
        List<String> autores,
        @Nullable Dinheiro precoUnitario,
        int quantidade,
        @Nullable Dinheiro subtotal) {

    public ItemComPreco {
        autores = List.copyOf(autores);
    }
}
