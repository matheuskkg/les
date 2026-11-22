package br.fatec.les.livraria.vendas.dominio.frete;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Blocos de peso do frete (RF-VEN-004, DEC-05.3): blocos iniciados de 0,5 kg, no mínimo 1. */
public final class BlocosDePeso {

    private static final BigDecimal BLOCO = new BigDecimal("0.5");

    private BlocosDePeso() {}

    /**
     * @param pesoTotal soma dos pesos considerados, em kg
     */
    public static int de(BigDecimal pesoTotal) {
        int blocos = pesoTotal.divide(BLOCO, 0, RoundingMode.CEILING).intValueExact();
        return Math.max(blocos, 1);
    }
}
