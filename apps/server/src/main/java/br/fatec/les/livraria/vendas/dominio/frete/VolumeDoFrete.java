package br.fatec.les.livraria.vendas.dominio.frete;

import java.math.BigDecimal;
import java.util.Objects;

/** Item da compra como o frete o enxerga (RF-VEN-004, DEC-05.3): peso real (kg), dimensões (cm) e quantidade. */
public record VolumeDoFrete(
        BigDecimal peso, BigDecimal altura, BigDecimal largura, BigDecimal profundidade, int quantidade) {

    public VolumeDoFrete {
        Objects.requireNonNull(peso, "peso");
        Objects.requireNonNull(altura, "altura");
        Objects.requireNonNull(largura, "largura");
        Objects.requireNonNull(profundidade, "profundidade");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade do volume deve ser no mínimo 1.");
        }
    }
}
