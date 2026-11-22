package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.util.List;
import java.util.Objects;

/**
 * Valores e cupons de uma compra, antes dos cartões (RF-VEN-007, DEC-11).
 *
 * @param promocional nulo se a compra não usar promocional
 */
public record CuponsDaCompra(
        Dinheiro subtotal, Dinheiro frete, CupomPromocional promocional, List<CupomTroca> cuponsDeTroca) {

    public CuponsDaCompra {
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(frete, "frete");
        cuponsDeTroca = List.copyOf(Objects.requireNonNull(cuponsDeTroca, "cuponsDeTroca"));
    }

    public Dinheiro total() {
        return subtotal.somar(frete);
    }

    public Dinheiro descontoPromocional() {
        return promocional == null ? Dinheiro.ZERO : promocional.desconto(subtotal, frete);
    }

    public boolean temCupom() {
        return promocional != null || !cuponsDeTroca.isEmpty();
    }
}
