package br.fatec.les.livraria.vendas.dominio.pagamento;

import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Como o total da compra é pago (RF-VEN-007, RN-VEN-006, DEC-11): o desconto do promocional, o que cada cupom de troca
 * cobriu, o troco gerado e o restante a pagar com cartões.
 *
 * @param comCupom se a compra usa algum cupom, o que libera a exceção do RN-VEN-005
 */
public record DistribuicaoDoPagamento(
        Dinheiro subtotal,
        Dinheiro frete,
        Dinheiro total,
        Dinheiro descontoPromocional,
        List<CupomAbatido> cuponsDeTroca,
        Dinheiro troco,
        Dinheiro restante,
        boolean comCupom) {

    public DistribuicaoDoPagamento {
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(frete, "frete");
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(descontoPromocional, "descontoPromocional");
        Objects.requireNonNull(troco, "troco");
        Objects.requireNonNull(restante, "restante");
        cuponsDeTroca = List.copyOf(Objects.requireNonNull(cuponsDeTroca, "cuponsDeTroca"));
    }
}
