package br.fatec.les.livraria.vendas.aplicacao.consulta;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Retrato do checkout aberto para a API (RF-VEN-003, DEC-07, DEC-12). {@code expiraEm} serve só para exibição (DEC-13).
 *
 * @param id id do checkout
 * @param itens itens com o preço de venda atual
 * @param subtotal soma dos subtotais
 * @param quantidadeDeItens soma das quantidades
 * @param iniciadoEm início do checkout
 * @param expiraEm fim do prazo
 */
public record CheckoutConsultado(
        Long id,
        List<ItemComPreco> itens,
        Dinheiro subtotal,
        int quantidadeDeItens,
        LocalDateTime iniciadoEm,
        LocalDateTime expiraEm) {

    public CheckoutConsultado {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(iniciadoEm, "iniciadoEm");
        Objects.requireNonNull(expiraEm, "expiraEm");
        itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
    }
}
