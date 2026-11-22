package br.fatec.les.livraria.vendas.aplicacao.consulta;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Livro retirado do carrinho por expiração, para exibição sem opção de compra (RNF-VEN-001, DEC-16).
 *
 * @param livroId código do livro
 * @param titulo título atual do livro; nulo se o livro não existe mais
 * @param autores autores atuais do livro
 * @param quantidade unidades que estavam no carrinho
 * @param removidoEm instante da retirada
 */
public record ItemRemovidoDoCarrinho(
        Long livroId, @Nullable String titulo, List<String> autores, int quantidade, LocalDateTime removidoEm) {

    public ItemRemovidoDoCarrinho {
        autores = List.copyOf(autores);
    }
}
