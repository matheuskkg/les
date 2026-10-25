package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.List;

/**
 * Métricas de funcionamento da IA no período, para a tela do ADMIN (RNF-REC-001, DEC-03).
 *
 * @param periodo            dias consultados
 * @param provedores         uma linha por provedor, na ordem da cadeia, inclusive os sem chamadas
 * @param recomendacoesSemIa recomendações da vitrine gravadas sem IA no período; não têm provedor
 * @param serie              chamadas e falhas por dia, com todos os dias do período
 */
public record MetricasDaIa(
        PeriodoDasMetricas periodo,
        List<MetricasDoProvedor> provedores,
        long recomendacoesSemIa,
        List<ChamadasNoDia> serie) {

    public MetricasDaIa {
        provedores = List.copyOf(provedores);
        serie = List.copyOf(serie);
    }
}
