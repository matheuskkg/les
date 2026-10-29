package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadasNoDia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.OperacaoIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;

class SerieDiariaDasChamadasTest {

    private static ChamadaIa chamada(Long provedor, LocalDateTime em, ResultadoDaChamada resultado) {
        return new ChamadaIa(provedor, "m", OperacaoIa.CONVERSAR, em, 10, 1, resultado, null, null, null, null, 0,
                null);
    }

    @Test
    void rnfRec001_somaOsProvedoresPorDiaComZeroNosDiasSemChamada() {
        LocalDate dia = LocalDate.of(2026, 10, 1);
        List<ChamadaIa> chamadas = List.of(
                chamada(1L, dia.atTime(0, 0), ResultadoDaChamada.COTA_ESGOTADA),
                chamada(2L, dia.atTime(23, 59), ResultadoDaChamada.SUCESSO),
                chamada(1L, dia.plusDays(2).atTime(12, 0), ResultadoDaChamada.SUCESSO));

        List<ChamadasNoDia> serie = new SerieDiariaDasChamadas()
                .calcular(new PeriodoDasMetricas(dia, dia.plusDays(3)), chamadas);

        assertThat(serie).extracting(ChamadasNoDia::dia, ChamadasNoDia::chamadas, ChamadasNoDia::falhas)
                .containsExactly(
                        tuple(dia, 2L, 1L),
                        tuple(dia.plusDays(1), 0L, 0L),
                        tuple(dia.plusDays(2), 1L, 0L),
                        tuple(dia.plusDays(3), 0L, 0L));
    }
}
