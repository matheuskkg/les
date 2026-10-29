package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Detecção da cota diária no 429 (RNF-REC-001, DEC-03, REC-09), que mantém o provedor fora da cadeia até o dia
 * seguinte.
 */
class DetectarCotaDiariaTest {

    @Test
    void rnfRec001_mensagemComPerDayEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria("You exceeded your quota per day", null)).isTrue();
    }

    @Test
    void rnfRec001_mensagemComFreeModelsPerDayEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria("free-models-per-day limit reached", null)).isTrue();
    }

    @Test
    void rnfRec001_mensagemComRpdEhCotaDiariaSemDiferenciarCaixa() {
        assertThat(DetectarCotaDiaria.ehDiaria("RPD limit reached", null)).isTrue();
    }

    @Test
    void rnfRec001_mensagemComDailyEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria("Daily quota exceeded", null)).isTrue();
    }

    @Test
    void rnfRec001_limitePorMinutoNaoEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria("Rate limit reached, requests per minute", null)).isFalse();
    }

    @Test
    void rnfRec001_esperaLongaComIndicacaoDeDiaEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria("The limit resets at the end of the day", Duration.ofHours(6)))
                .isTrue();
    }

    @Test
    void rnfRec001_esperaLongaSemIndicacaoDeDiaNaoEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria("The limit resets later", Duration.ofHours(6))).isFalse();
    }

    @Test
    void rnfRec001_mensagemNulaNaoEhCotaDiaria() {
        assertThat(DetectarCotaDiaria.ehDiaria(null, Duration.ofHours(6))).isFalse();
    }
}
