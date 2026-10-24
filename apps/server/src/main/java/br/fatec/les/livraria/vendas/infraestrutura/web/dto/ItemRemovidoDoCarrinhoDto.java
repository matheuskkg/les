package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemRemovidoDoCarrinho;

/**
 * Livro retirado do carrinho por expiração, na API (RNF-VEN-001, DEC-07). Não tem preço: para comprá-lo, o cliente o
 * adiciona de novo. {@code removidoEm} em UTC.
 */
public record ItemRemovidoDoCarrinhoDto(
        Long livroId,
        @Nullable String titulo,
        List<String> autores,
        int quantidade,
        Instant removidoEm) {

    public static ItemRemovidoDoCarrinhoDto de(ItemRemovidoDoCarrinho removido) {
        return new ItemRemovidoDoCarrinhoDto(removido.livroId(), removido.titulo(), removido.autores(),
                removido.quantidade(), removido.removidoEm().atZone(RelogioDoSistema.FUSO).toInstant());
    }
}
