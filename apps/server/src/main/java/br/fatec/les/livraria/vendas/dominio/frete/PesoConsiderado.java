package br.fatec.les.livraria.vendas.dominio.frete;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Peso considerado de um item no frete (RF-VEN-004, DEC-05.3): o maior entre o peso real e o peso cúbico {@code altura
 * × largura × profundidade / 6000}, de uma unidade, multiplicado pela quantidade.
 */
public final class PesoConsiderado {

    private static final BigDecimal FATOR_CUBICO = new BigDecimal("6000");
    private static final int ESCALA = 10;

    private PesoConsiderado() {}

    /**
     * @return peso considerado do item, em kg
     */
    public static BigDecimal de(VolumeDoFrete volume) {
        BigDecimal cubico =
                volume.altura()
                        .multiply(volume.largura())
                        .multiply(volume.profundidade())
                        .divide(FATOR_CUBICO, ESCALA, RoundingMode.HALF_UP);
        return volume.peso().max(cubico).multiply(BigDecimal.valueOf(volume.quantidade()));
    }
}
