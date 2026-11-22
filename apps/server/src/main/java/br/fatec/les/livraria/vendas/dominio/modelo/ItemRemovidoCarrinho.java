package br.fatec.les.livraria.vendas.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Livro retirado do carrinho porque o prazo do bloqueio venceu (RN-VEN-014, RN-VEN-015, RNF-VEN-001). Fica à mostra no
 * carrinho, sem opção de compra, até o cliente adicionar o livro de novo ou dispensar o aviso (DEC-16).
 *
 * @param livroId livro retirado
 * @param quantidade unidades que estavam no carrinho
 * @param removidoEm instante da retirada
 */
public record ItemRemovidoCarrinho(Long livroId, int quantidade, LocalDateTime removidoEm) {

    public ItemRemovidoCarrinho {
        Objects.requireNonNull(livroId, "livroId");
        Objects.requireNonNull(removidoEm, "removidoEm");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade removida deve ser no mínimo 1.");
        }
    }
}
