package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Espera de um provedor que respondeu 429 (RNF-REC-001, DEC-03, REC-09): tempo sugerido limitado entre 10 s e 5 min,
 * 5 min sem indicação e até a meia-noite na cota diária.
 */
class CalcularEsperaDeCotaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 28, 10, 0);

    @Test
    void rnfRec001_esperaSeteSegundosViraDezSegundos() {
        assertThat(CalcularEsperaDeCota.calcular(Duration.ofSeconds(7), false, AGORA))
                .isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void rnfRec001_esperaTrintaSegundosEhMantida() {
        assertThat(CalcularEsperaDeCota.calcular(Duration.ofSeconds(30), false, AGORA))
                .isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void rnfRec001_esperaDeQuinzeMinutosViraCincoMinutos() {
        assertThat(CalcularEsperaDeCota.calcular(Duration.ofMinutes(15), false, AGORA))
                .isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void rnfRec001_semInformacaoUsaCincoMinutos() {
        assertThat(CalcularEsperaDeCota.calcular(null, false, AGORA)).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void rnfRec001_limitesDeDezSegundosECincoMinutosSaoMantidos() {
        assertThat(CalcularEsperaDeCota.calcular(Duration.ofSeconds(10), false, AGORA))
                .isEqualTo(Duration.ofSeconds(10));
        assertThat(CalcularEsperaDeCota.calcular(Duration.ofMinutes(5), false, AGORA))
                .isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void rnfRec001_cotaDiariaEsperaAteAMeiaNoite() {
        LocalDateTime noite = LocalDateTime.of(2026, 10, 28, 22, 15);

        assertThat(CalcularEsperaDeCota.calcular(Duration.ofSeconds(30), true, noite))
                .isEqualTo(Duration.ofMinutes(105));
    }

    @Test
    void rnfRec001_cotaDiariaIgnoraOTempoSugerido() {
        assertThat(CalcularEsperaDeCota.calcular(Duration.ofHours(10), true, AGORA))
                .isEqualTo(Duration.ofHours(14));
    }
}
