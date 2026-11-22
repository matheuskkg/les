package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sessão de compra aberta do cliente, informada junto do carrinho para retomar a compra (RF-VEN-003, DEC-12). O prazo
 * serve só para exibição (DEC-13).
 *
 * @param expiraEm fim do prazo do checkout
 * @param quantidadeDeItens soma das quantidades dos itens do checkout
 */
public record CheckoutEmAndamento(LocalDateTime expiraEm, int quantidadeDeItens) {

    public CheckoutEmAndamento {
        Objects.requireNonNull(expiraEm, "expiraEm");
    }
}
