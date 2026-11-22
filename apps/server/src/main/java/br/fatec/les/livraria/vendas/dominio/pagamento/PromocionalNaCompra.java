package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Promocional informado numa compra, com o que a validação precisa saber (RN-VEN-007, DEC-11, DEC-15.2).
 *
 * @param jaUsado se o cliente já tem um uso registrado deste promocional
 */
public record PromocionalNaCompra(CupomPromocional cupom, Dinheiro subtotal, LocalDate hoje, boolean jaUsado) {

    public PromocionalNaCompra {
        Objects.requireNonNull(cupom, "cupom");
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(hoje, "hoje");
    }
}
