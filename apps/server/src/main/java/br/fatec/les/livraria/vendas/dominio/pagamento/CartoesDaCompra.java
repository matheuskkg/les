package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.util.List;
import java.util.Objects;

/**
 * Cartões informados para pagar o restante da compra, depois dos cupons (RN-VEN-004, RN-VEN-005, DEC-11).
 *
 * @param restante o que os cupons não cobriram
 * @param comCupom se a compra usa algum cupom, o que libera a exceção do RN-VEN-005
 */
public record CartoesDaCompra(Dinheiro restante, boolean comCupom, List<PagamentoEmCartao> cartoes) {

    public CartoesDaCompra {
        Objects.requireNonNull(restante, "restante");
        cartoes = List.copyOf(Objects.requireNonNull(cartoes, "cartoes"));
    }
}
