package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Leitura do tempo sugerido no 429 (RNF-REC-001, DEC-03, REC-09): cabeçalho {@code Retry-After} em segundos ou data
 * HTTP e trechos da mensagem com {@code ms}, {@code s}, {@code m} e {@code h}.
 */
class InterpretarEsperaSugeridaTest {

    private static final Instant AGORA = Instant.parse("2026-10-28T10:00:00Z");

    @Test
    void rnfRec001_cabecalhoEmSegundosAceitaDecimal() {
        assertThat(InterpretarEsperaSugerida.deCabecalho("7.5", AGORA)).contains(Duration.ofMillis(7_500));
        assertThat(InterpretarEsperaSugerida.deCabecalho("30", AGORA)).contains(Duration.ofSeconds(30));
    }

    @Test
    void rnfRec001_cabecalhoComDataHttpUsaAgora() {
        assertThat(InterpretarEsperaSugerida.deCabecalho("Wed, 28 Oct 2026 10:01:30 GMT", AGORA))
                .contains(Duration.ofSeconds(90));
    }

    @Test
    void rnfRec001_cabecalhoVazioOuInvalidoNaoInformaEspera() {
        assertThat(InterpretarEsperaSugerida.deCabecalho(null, AGORA)).isEmpty();
        assertThat(InterpretarEsperaSugerida.deCabecalho("  ", AGORA)).isEmpty();
        assertThat(InterpretarEsperaSugerida.deCabecalho("amanhã", AGORA)).isEmpty();
    }

    @Test
    void rnfRec001_mensagemComMinutosESegundosSomaAsPartes() {
        assertThat(InterpretarEsperaSugerida.daMensagem("Please try again in 1m30s."))
                .contains(Duration.ofSeconds(90));
    }

    @Test
    void rnfRec001_mensagemComSegundosDecimais() {
        assertThat(InterpretarEsperaSugerida.daMensagem("try again in 7.5s"))
                .contains(Duration.ofMillis(7_500));
    }

    @Test
    void rnfRec001_mensagemComMilissegundos() {
        assertThat(InterpretarEsperaSugerida.daMensagem("retry in 250ms")).contains(Duration.ofMillis(250));
    }

    @Test
    void rnfRec001_mensagemComSegundosInteiros() {
        assertThat(InterpretarEsperaSugerida.daMensagem("Rate limit reached, retry in 13s"))
                .contains(Duration.ofSeconds(13));
    }

    @Test
    void rnfRec001_mensagemSemIndicacaoNaoInformaEspera() {
        assertThat(InterpretarEsperaSugerida.daMensagem("Rate limit reached")).isEmpty();
        assertThat(InterpretarEsperaSugerida.daMensagem(null)).isEmpty();
    }
}
