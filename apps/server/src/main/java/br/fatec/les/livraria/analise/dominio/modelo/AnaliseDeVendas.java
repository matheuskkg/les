package br.fatec.les.livraria.analise.dominio.modelo;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Resultado da análise de vendas, base do gráfico e da exportação (RF-ANA-001, RF-ANA-004).
 *
 * @param periodo meses analisados
 * @param series uma linha por categoria selecionada, na ordem da seleção
 */
public record AnaliseDeVendas(PeriodoDeAnalise periodo, List<SerieDeVendas> series) {

    public AnaliseDeVendas {
        Objects.requireNonNull(periodo, "periodo");
        series = List.copyOf(series);
    }

    /**
     * Soma as vendas por mês e categoria (RN-ANA-001) e completa com R$ 0,00 os meses sem venda de cada categoria
     * selecionada (RN-ANA-003). Um item com várias categorias entra com o valor cheio em cada uma (DEC-12); vendas
     * fora do período ou de categorias não selecionadas são ignoradas.
     */
    public static AnaliseDeVendas calcular(PeriodoDeAnalise periodo, List<CategoriaAnalisada> categorias,
            List<VendaNoMes> vendas) {
        List<YearMonth> meses = periodo.meses();
        Map<Long, Map<YearMonth, Dinheiro>> totais = new HashMap<>();
        for (VendaNoMes venda : vendas) {
            for (Long categoria : venda.categorias()) {
                totais.computeIfAbsent(categoria, id -> new HashMap<>())
                        .merge(venda.mes(), venda.valor(), Dinheiro::somar);
            }
        }
        List<SerieDeVendas> series = new ArrayList<>();
        for (CategoriaAnalisada categoria : categorias) {
            Map<YearMonth, Dinheiro> porMes = totais.getOrDefault(categoria.id(), Map.of());
            series.add(new SerieDeVendas(categoria,
                    meses.stream().map(mes -> porMes.getOrDefault(mes, Dinheiro.ZERO)).toList()));
        }
        return new AnaliseDeVendas(periodo, series);
    }
}
