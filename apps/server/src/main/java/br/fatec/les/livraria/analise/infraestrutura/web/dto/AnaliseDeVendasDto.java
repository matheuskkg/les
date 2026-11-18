package br.fatec.les.livraria.analise.infraestrutura.web.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import br.fatec.les.livraria.analise.dominio.modelo.AnaliseDeVendas;
import br.fatec.les.livraria.analise.dominio.modelo.SerieDeVendas;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Dados do gráfico de evolução de vendas (RF-ANA-001, RNF-ANA-001): as datas exatas do filtro, os meses do eixo X e
 * uma série por categoria, com um valor para cada mês, na mesma ordem.
 */
public record AnaliseDeVendasDto(LocalDate inicio, LocalDate fim, List<YearMonth> meses, List<SerieDeVendasDto> series) {

    /** Linha de uma categoria; meses sem venda valem {@code 0.00} (RN-ANA-003). */
    public record SerieDeVendasDto(Long categoriaId, String categoria, List<Dinheiro> valores) {

        static SerieDeVendasDto de(SerieDeVendas serie) {
            return new SerieDeVendasDto(serie.categoria().id(), serie.categoria().nome(), serie.valores());
        }
    }

    public static AnaliseDeVendasDto de(AnaliseDeVendas analise) {
        return new AnaliseDeVendasDto(analise.periodo().inicio(), analise.periodo().fim(), analise.periodo().meses(),
                analise.series().stream().map(SerieDeVendasDto::de).toList());
    }
}
