package br.fatec.les.livraria.clientes.dominio.ranking;

import java.time.LocalDate;

/**
 * Janela de {@value #DIAS} dias até a data do cálculo (RN-CLI-007, DEC-04): contam as compras a partir do dia
 * devolvido, inclusive, como a faixa "271 a 365" dias da recência.
 */
public final class JanelaDoRanking {

    public static final int DIAS = 365;

    private JanelaDoRanking() {
    }

    public static LocalDate inicio(LocalDate hoje) {
        return hoje.minusDays(DIAS);
    }
}
