package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Frete do checkout para o endereço de entrega escolhido (RF-VEN-004, RF-VEN-005, DEC-05.3).
 *
 * @param frete    valor do frete
 * @param subtotal soma dos itens, com o preço de venda atual
 * @param total    subtotal mais frete, antes de cupons
 */
public record FreteCalculado(Dinheiro frete, Dinheiro subtotal, Dinheiro total) {

    public FreteCalculado {
        Objects.requireNonNull(frete, "frete");
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(total, "total");
    }
}
