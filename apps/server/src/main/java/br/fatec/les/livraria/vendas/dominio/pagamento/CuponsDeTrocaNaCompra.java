package br.fatec.les.livraria.vendas.dominio.pagamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Cupons de troca escolhidos numa compra, já confirmados como do cliente (RN-VEN-007, DEC-11). */
public record CuponsDeTrocaNaCompra(List<CupomTroca> cupons, LocalDate hoje) {

    public CuponsDeTrocaNaCompra {
        cupons = List.copyOf(Objects.requireNonNull(cupons, "cupons"));
        Objects.requireNonNull(hoje, "hoje");
    }
}
