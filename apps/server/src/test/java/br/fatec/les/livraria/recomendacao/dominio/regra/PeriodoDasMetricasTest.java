package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;

/** Regras do período das métricas da IA: {@link FimDasMetricasNaoAnteriorAoInicio} e o máximo de 366 dias. */
class PeriodoDasMetricasTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);

    private Violacoes validar(LocalDate inicio, LocalDate fim) {
        Violacoes violacoes = new Violacoes();
        PeriodoDasMetricas periodo = new PeriodoDasMetricas(inicio, fim);
        new FimDasMetricasNaoAnteriorAoInicio().validar(periodo, violacoes);
        new PeriodoDasMetricasDeAte366Dias().validar(periodo, violacoes);
        return violacoes;
    }

    @Test
    void rnfRec001_aceitaUmDiaEAte366Dias() {
        assertThat(validar(INICIO, INICIO).vazia()).isTrue();
        assertThat(validar(INICIO, INICIO.plusDays(365)).vazia()).isTrue();
    }

    @Test
    void rnfRec001_recusaMaisDe366Dias() {
        assertThat(validar(INICIO, INICIO.plusDays(366)).lista()).extracting(Violacao::mensagem)
                .containsExactly("O período das métricas pode ter até 366 dias.");
    }

    @Test
    void rnfRec001_recusaFimAnteriorAoInicio() {
        assertThat(validar(INICIO, INICIO.minusDays(1)).lista()).extracting(Violacao::mensagem)
                .containsExactly("O dia final não pode ser anterior ao inicial.");
    }
}
