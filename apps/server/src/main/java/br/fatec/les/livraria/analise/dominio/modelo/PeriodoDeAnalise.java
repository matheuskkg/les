package br.fatec.les.livraria.analise.dominio.modelo;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Período da análise em meses, com o inicial e o final inclusive (RF-ANA-002, RN-ANA-001).
 *
 * @param inicio primeiro mês
 * @param fim último mês, não anterior ao primeiro
 */
public record PeriodoDeAnalise(YearMonth inicio, YearMonth fim) {

    public PeriodoDeAnalise {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim do período é anterior ao início.");
        }
    }

    /** Os meses do período, em ordem, um ponto do gráfico para cada (RN-ANA-001). */
    public List<YearMonth> meses() {
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = inicio; !mes.isAfter(fim); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    /** Início do primeiro dia do primeiro mês, no fuso da loja. */
    public LocalDateTime primeiroInstante() {
        return inicio.atDay(1).atStartOfDay();
    }

    /** Início do mês seguinte ao último, exclusive. */
    public LocalDateTime fimExclusivo() {
        return fim.plusMonths(1).atDay(1).atStartOfDay();
    }
}
