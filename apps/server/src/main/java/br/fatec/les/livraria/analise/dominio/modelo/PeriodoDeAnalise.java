package br.fatec.les.livraria.analise.dominio.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Período da análise entre duas datas, com o inicial e o final inclusive (RF-ANA-002). O filtro é por dia, mas os
 * pontos do gráfico continuam sendo os meses tocados pelo intervalo (RN-ANA-001, RNF-ANA-001): um mês parcial entra com
 * as vendas dos dias selecionados.
 *
 * @param inicio primeiro dia do período
 * @param fim último dia do período, não anterior ao primeiro
 */
public record PeriodoDeAnalise(LocalDate inicio, LocalDate fim) {

    public PeriodoDeAnalise {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim do período é anterior ao início.");
        }
    }

    /** Os meses tocados pelo período, em ordem, um ponto do gráfico para cada (RN-ANA-001). */
    public List<YearMonth> meses() {
        YearMonth primeiro = YearMonth.from(inicio);
        YearMonth ultimo = YearMonth.from(fim);
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = primeiro; !mes.isAfter(ultimo); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    /** Meia-noite do primeiro dia selecionado, no fuso da loja. */
    public LocalDateTime primeiroInstante() {
        return inicio.atStartOfDay();
    }

    /** Meia-noite do dia seguinte ao último selecionado, exclusiva. */
    public LocalDateTime fimExclusivo() {
        return fim.plusDays(1).atStartOfDay();
    }
}
