package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.Duration;

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
    void rnfRec001_idDeCotaDiariaDoGeminiEhCotaDiaria() {
        assertThat(
                        DetectarCotaDiaria.ehDiaria(
                                "quotaId: GenerateRequestsPerDayPerProjectPerModel-FreeTier. Please retry in 41.9s",
                                null))
                .isTrue();
    }

    @Test
    void rnfRec001_palavrasComDiaNoMeioNaoSaoCotaDiaria() {
        assertThat(
                        DetectarCotaDiaria.ehDiaria(
                                "Try again in 10m or upgrade today, immediately", Duration.ofMinutes(10)))
                .isFalse();
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
