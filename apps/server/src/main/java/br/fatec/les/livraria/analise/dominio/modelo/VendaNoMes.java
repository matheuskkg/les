package br.fatec.les.livraria.analise.dominio.modelo;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

/**
 * Valor vendido de um item num mês, já sem a quantidade trocada (RN-ANA-001, RN-ANA-004, DEC-12).
 *
 * @param mes mês da finalização do pedido
 * @param categorias ids das categorias copiadas no pedido; o valor entra cheio em cada uma
 * @param valor preço unitário × quantidade não trocada
 */
public record VendaNoMes(YearMonth mes, Set<Long> categorias, Dinheiro valor) {

    public VendaNoMes {
        Objects.requireNonNull(mes, "mes");
        Objects.requireNonNull(valor, "valor");
        categorias = Set.copyOf(categorias);
    }
}
