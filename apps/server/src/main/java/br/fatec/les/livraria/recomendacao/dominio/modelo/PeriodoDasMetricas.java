package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Período das métricas da IA em dias, com o inicial e o final inclusive (RNF-REC-001, DEC-03). As regras
 * {@code FimDasMetricasNaoAnteriorAoInicio} e {@code PeriodoDasMetricasDeAte366Dias} validam o intervalo.
 *
 * @param inicio primeiro dia
 * @param fim    último dia
 */
public record PeriodoDasMetricas(LocalDate inicio, LocalDate fim) {

    public PeriodoDasMetricas {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
    }

    /** Quantidade de dias, contando o inicial e o final; zero ou negativa quando o fim é anterior ao início. */
    public long quantidadeDeDias() {
        return ChronoUnit.DAYS.between(inicio, fim) + 1;
    }

    /** Os dias do período, em ordem, um ponto do gráfico para cada. */
    public List<LocalDate> dias() {
        return inicio.datesUntil(fim.plusDays(1)).toList();
    }

    /** Início do primeiro dia. */
    public LocalDateTime primeiroInstante() {
        return inicio.atStartOfDay();
    }

    /** Início do dia seguinte ao último, exclusive. */
    public LocalDateTime fimExclusivo() {
        return fim.plusDays(1).atStartOfDay();
    }
}
