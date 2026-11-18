package br.fatec.les.livraria.analise.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.analise.dominio.modelo.AnaliseDeVendas;
import br.fatec.les.livraria.analise.dominio.modelo.CategoriaAnalisada;
import br.fatec.les.livraria.analise.dominio.modelo.PeriodoDeAnalise;
import br.fatec.les.livraria.analise.dominio.modelo.VendaNoMes;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

class PlanilhaDaAnaliseTest {

    @Test
    void rfAna004_csvComBomPontoEVirgulaEVirgulaDecimalSemMilhar_DEC_15_4() {
        YearMonth janeiro = YearMonth.of(2025, 1);
        AnaliseDeVendas analise = AnaliseDeVendas.calcular(
                new PeriodoDeAnalise(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 28)),
                List.of(new CategoriaAnalisada(2L, "Ficção Científica"), new CategoriaAnalisada(9L, "Contos; \"curtos\"")),
                List.of(new VendaNoMes(janeiro, Set.of(2L), Dinheiro.de("12345.60"))));

        byte[] csv = PlanilhaDaAnalise.csv(analise);

        assertThat(csv).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(new String(csv, StandardCharsets.UTF_8)).isEqualTo("﻿" + """
                mes;categoria;valor\r
                2025-01;Ficção Científica;12345,60\r
                2025-01;"Contos; ""curtos\""";0,00\r
                2025-02;Ficção Científica;0,00\r
                2025-02;"Contos; ""curtos\""";0,00\r
                """);
    }

    @Test
    void rfAna004_nomeQueComecaComoFormulaGanhaApostrofo() {
        AnaliseDeVendas analise = AnaliseDeVendas.calcular(
                new PeriodoDeAnalise(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 20)),
                List.of(new CategoriaAnalisada(1L, "=1+1"), new CategoriaAnalisada(2L, "-Ofertas")), List.of());

        assertThat(new String(PlanilhaDaAnalise.csv(analise), StandardCharsets.UTF_8))
                .contains("2025-01;'=1+1;0,00\r\n", "2025-01;'-Ofertas;0,00\r\n");
    }
}
