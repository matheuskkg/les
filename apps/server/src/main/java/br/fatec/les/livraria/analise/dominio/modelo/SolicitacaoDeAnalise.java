package br.fatec.les.livraria.analise.dominio.modelo;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Filtros da análise de vendas informados pelo admin (RF-ANA-001 a RF-ANA-003), ainda não validados. As datas são
 * exatas e inclusivas; o agrupamento do resultado continua mensal (RN-ANA-001).
 *
 * @param inicio primeiro dia do período, inclusive
 * @param fim último dia do período, inclusive
 * @param categorias ids das categorias selecionadas, sem repetição e na ordem informada
 */
public record SolicitacaoDeAnalise(LocalDate inicio, LocalDate fim, List<Long> categorias) {

    public SolicitacaoDeAnalise {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        categorias = categorias == null ? List.of()
                : categorias.stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * Quantidade de meses calendário tocados pelo período, contando o mês do início e o do fim (RN-ANA-002). Usa
     * aritmética de {@link YearMonth} em vez de contar dias: assim 29/02/2024 a 28/02/2025 é um ano (12 meses), e não
     * um número de dias que muda com anos bissextos ou com o tamanho de cada mês.
     */
    public long quantidadeDeMeses() {
        return YearMonth.from(inicio).until(YearMonth.from(fim), ChronoUnit.MONTHS) + 1;
    }
}
