package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;

/**
 * Pagamento conferido (RF-VEN-006, RF-VEN-007, DEC-11): a distribuição entre promocional, cupons de troca e cartões.
 * Sem cartões informados, mostra o restante que eles terão de pagar.
 */
public record PagamentoConferido(DistribuicaoDoPagamento distribuicao, List<CartaoNoPagamento> cartoes) {

    public PagamentoConferido {
        Objects.requireNonNull(distribuicao, "distribuicao");
        cartoes = List.copyOf(Objects.requireNonNull(cartoes, "cartoes"));
    }
}
