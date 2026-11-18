package br.fatec.les.livraria.analise.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

class AnaliseDeVendasTest {

    private static final YearMonth JANEIRO = YearMonth.of(2025, 1);
    private static final YearMonth FEVEREIRO = YearMonth.of(2025, 2);
    private static final YearMonth MARCO = YearMonth.of(2025, 3);
    private static final CategoriaAnalisada ROMANCE = new CategoriaAnalisada(1L, "Romance");
    private static final CategoriaAnalisada HISTORIA = new CategoriaAnalisada(5L, "História");
    private static final CategoriaAnalisada TECNOLOGIA = new CategoriaAnalisada(4L, "Tecnologia");

    @Test
    void rnAna001_somaAsVendasDeCadaCategoriaPorMes() {
        AnaliseDeVendas analise = AnaliseDeVendas.calcular(periodo(JANEIRO, FEVEREIRO), List.of(ROMANCE),
                List.of(venda(JANEIRO, "10.00", 1L), venda(JANEIRO, "5.50", 1L), venda(FEVEREIRO, "7.25", 1L)));

        assertThat(analise.series()).singleElement()
                .satisfies(serie -> assertThat(serie.valores()).containsExactly(Dinheiro.de("15.50"),
                        Dinheiro.de("7.25")));
    }

    @Test
    void rnAna001_itemComVariasCategoriasEntraComValorCheioEmCadaUma_DEC_12() {
        AnaliseDeVendas analise = AnaliseDeVendas.calcular(periodo(MARCO, MARCO),
                List.of(ROMANCE, HISTORIA), List.of(venda(MARCO, "95.76", 1L, 5L)));

        assertThat(analise.series()).extracting(SerieDeVendas::valores)
                .containsExactly(List.of(Dinheiro.de("95.76")), List.of(Dinheiro.de("95.76")));
    }

    @Test
    void rnAna003_mesesSemVendaValemZeroNaOrdemDosMeses() {
        AnaliseDeVendas analise = AnaliseDeVendas.calcular(periodo(JANEIRO, MARCO),
                List.of(TECNOLOGIA, ROMANCE), List.of(venda(FEVEREIRO, "20.00", 1L)));

        assertThat(analise.series()).extracting(serie -> serie.categoria().nome())
                .containsExactly("Tecnologia", "Romance");
        assertThat(analise.series().get(0).valores()).containsOnly(Dinheiro.ZERO).hasSize(3);
        assertThat(analise.series().get(1).valores())
                .containsExactly(Dinheiro.ZERO, Dinheiro.de("20.00"), Dinheiro.ZERO);
    }

    @Test
    void rfAna003_ignoraCategoriasNaoSelecionadasEMesesForaDoPeriodo() {
        AnaliseDeVendas analise = AnaliseDeVendas.calcular(periodo(FEVEREIRO, FEVEREIRO),
                List.of(ROMANCE), List.of(venda(JANEIRO, "1.00", 1L), venda(FEVEREIRO, "2.00", 4L)));

        assertThat(analise.series().getFirst().valores()).containsExactly(Dinheiro.ZERO);
    }

    @Test
    void rfAna002_periodoTemOsMesesInicialEFinalEOsInstantesDoFusoDaLoja() {
        PeriodoDeAnalise periodo = new PeriodoDeAnalise(LocalDate.of(2024, 12, 15), LocalDate.of(2025, 2, 10));

        assertThat(periodo.meses()).containsExactly(YearMonth.of(2024, 12), JANEIRO, FEVEREIRO);
        assertThat(periodo.primeiroInstante()).isEqualTo(LocalDateTime.of(2024, 12, 15, 0, 0));
        assertThat(periodo.fimExclusivo()).isEqualTo(LocalDateTime.of(2025, 2, 11, 0, 0));
        assertThatThrownBy(() -> new PeriodoDeAnalise(LocalDate.of(2025, 2, 10), LocalDate.of(2025, 1, 15)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rfAna002_umMesParcialContinuaSendoUmPontoNoGrafico_DEC_19() {
        PeriodoDeAnalise periodo = new PeriodoDeAnalise(LocalDate.of(2025, 3, 12), LocalDate.of(2025, 3, 20));

        assertThat(periodo.meses()).containsExactly(MARCO);
        assertThat(periodo.primeiroInstante()).isEqualTo(LocalDateTime.of(2025, 3, 12, 0, 0));
        assertThat(periodo.fimExclusivo()).isEqualTo(LocalDateTime.of(2025, 3, 21, 0, 0));
    }

    private static PeriodoDeAnalise periodo(YearMonth inicio, YearMonth fim) {
        return new PeriodoDeAnalise(inicio.atDay(1), fim.atEndOfMonth());
    }

    private static VendaNoMes venda(YearMonth mes, String valor, Long... categorias) {
        return new VendaNoMes(mes, Set.of(categorias), Dinheiro.de(valor));
    }
}
