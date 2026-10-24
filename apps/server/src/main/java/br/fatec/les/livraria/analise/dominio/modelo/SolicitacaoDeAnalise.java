package br.fatec.les.livraria.analise.dominio.modelo;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Filtros da análise de vendas informados pelo admin (RF-ANA-001 a RF-ANA-003), ainda não validados.
 *
 * @param inicio primeiro mês do período, inclusive
 * @param fim último mês do período, inclusive
 * @param categorias ids das categorias selecionadas, sem repetição e na ordem informada
 */
public record SolicitacaoDeAnalise(YearMonth inicio, YearMonth fim, List<Long> categorias) {

    public SolicitacaoDeAnalise {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        categorias = categorias == null ? List.of()
                : categorias.stream().filter(Objects::nonNull).distinct().toList();
    }

    /** Quantidade de meses do período, contando o inicial e o final (RN-ANA-002). */
    public long quantidadeDeMeses() {
        return inicio.until(fim, ChronoUnit.MONTHS) + 1;
    }
}
