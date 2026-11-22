package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.util.Objects;

/** Cupom de troca usado na compra e quanto dele cobriu o total (DEC-11); o que passar vira troco. */
public record CupomAbatido(CupomTroca cupom, Dinheiro abatido) {

    public CupomAbatido {
        Objects.requireNonNull(cupom, "cupom");
        Objects.requireNonNull(abatido, "abatido");
    }
}
