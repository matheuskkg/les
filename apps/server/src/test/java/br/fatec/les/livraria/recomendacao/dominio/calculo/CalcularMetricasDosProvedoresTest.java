package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDeChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MetricasDoProvedor;
import br.fatec.les.livraria.recomendacao.dominio.modelo.OperacaoIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

class CalcularMetricasDosProvedoresTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 25, 12, 0);
    private static final List<ProvedorIa> PROVEDORES =
            List.of(new ProvedorIa(1L, "gemini", 1, true), new ProvedorIa(4L, "fake", 2, true));

    private final CalcularMetricasDosProvedores calcular = new CalcularMetricasDosProvedores(new Percentil95());

    private static ChamadaIa chamada(
            Long provedor,
            int minuto,
            int latencia,
            int tentativa,
            ResultadoDaChamada resultado,
            Integer codigoHttp,
            Integer tokensEntrada,
            Integer tokensSaida) {
        return chamada(provedor, minuto, latencia, tentativa, resultado, codigoHttp, tokensEntrada, tokensSaida, 0);
    }

    private static ChamadaIa chamada(
            Long provedor,
            int minuto,
            int latencia,
            int tentativa,
            ResultadoDaChamada resultado,
            Integer codigoHttp,
            Integer tokensEntrada,
            Integer tokensSaida,
            int chamadasHttp) {
        return new ChamadaIa(
                provedor,
                "m",
                OperacaoIa.CONVERSAR,
                AGORA.plusMinutes(minuto),
                latencia,
                tentativa,
                resultado,
                codigoHttp,
                "erro",
                tokensEntrada,
                tokensSaida,
                chamadasHttp,
                null);
    }

    @Test
    void rnfRec001_calculaAsMetricasDeCadaProvedor() {
        List<ChamadaIa> chamadas =
                List.of(
                        chamada(1L, 0, 900, 1, ResultadoDaChamada.COTA_ESGOTADA, 429, null, null),
                        chamada(1L, 5, 800, 1, ResultadoDaChamada.ERRO_HTTP, 500, null, null),
                        chamada(1L, 9, 100, 1, ResultadoDaChamada.SUCESSO, null, 50, 10),
                        chamada(1L, 2, 700, 1, ResultadoDaChamada.COTA_ESGOTADA, 429, null, null),
                        chamada(4L, 1, 10, 2, ResultadoDaChamada.SUCESSO, null, null, null),
                        chamada(4L, 6, 30, 1, ResultadoDaChamada.SUCESSO, null, 7, 3),
                        chamada(4L, 8, 21, 3, ResultadoDaChamada.SUCESSO, null, null, null));
        List<AvaliacaoDeChamada> avaliacoes =
                List.of(
                        new AvaliacaoDeChamada(4L, Avaliacao.POSITIVA),
                        new AvaliacaoDeChamada(4L, Avaliacao.NEGATIVA),
                        new AvaliacaoDeChamada(4L, Avaliacao.POSITIVA),
                        new AvaliacaoDeChamada(4L, Avaliacao.POSITIVA));

        List<MetricasDoProvedor> metricas = calcular.calcular(PROVEDORES, chamadas, avaliacoes);

        MetricasDoProvedor gemini = metricas.get(0);
        assertThat(gemini.nome()).isEqualTo("gemini");
        assertThat(gemini.chamadas()).isEqualTo(4);
        assertThat(gemini.sucessos()).isEqualTo(1);
        assertThat(gemini.taxaDeSucesso()).isEqualTo(0.25);
        assertThat(gemini.falhasPorResultado())
                .isEqualTo(Map.of(ResultadoDaChamada.COTA_ESGOTADA, 2L, ResultadoDaChamada.ERRO_HTTP, 1L));
        assertThat(gemini.ultimaFalha())
                .isEqualTo(new UltimaFalha(AGORA.plusMinutes(5), ResultadoDaChamada.ERRO_HTTP, 500));
        assertThat(gemini.latenciaMediaMs()).isEqualTo(100);
        assertThat(gemini.latenciaP95Ms()).isEqualTo(100);
        assertThat(gemini.tokensEntrada()).isEqualTo(50);
        assertThat(gemini.tokensSaida()).isEqualTo(10);
        assertThat(gemini.mediaDeChamadasHttp()).isNull();
        assertThat(gemini.maximoDeChamadasHttp()).isNull();
        assertThat(gemini.atendimentosPorReserva()).isZero();
        assertThat(gemini.taxaDeAprovacao()).isNull();

        MetricasDoProvedor fake = metricas.get(1);
        assertThat(fake.taxaDeSucesso()).isEqualTo(1.0);
        assertThat(fake.falhasPorResultado()).isEmpty();
        assertThat(fake.ultimaFalha()).isNull();
        assertThat(fake.latenciaMediaMs()).isEqualTo(20);
        assertThat(fake.latenciaP95Ms()).isEqualTo(30);
        assertThat(fake.tokensEntrada()).isEqualTo(7);
        assertThat(fake.atendimentosPorReserva()).isEqualTo(2);
        assertThat(fake.avaliadas()).isEqualTo(4);
        assertThat(fake.positivas()).isEqualTo(3);
        assertThat(fake.taxaDeAprovacao()).isEqualTo(0.75);
    }

    @Test
    void rnfRec001_provedorSemChamadasApareceSemTaxasNemLatencias() {
        MetricasDoProvedor gemini = calcular.calcular(PROVEDORES, List.of(), List.of()).getFirst();

        assertThat(gemini.chamadas()).isZero();
        assertThat(gemini.taxaDeSucesso()).isNull();
        assertThat(gemini.latenciaMediaMs()).isNull();
        assertThat(gemini.latenciaP95Ms()).isNull();
        assertThat(gemini.mediaDeChamadasHttp()).isNull();
        assertThat(gemini.maximoDeChamadasHttp()).isNull();
        assertThat(gemini.taxaDeAprovacao()).isNull();
    }

    @Test
    void rnfRec001_mediaEMaximoDeChamadasHttpIgnoramAsTentativasSemChamada() {
        List<ChamadaIa> chamadas =
                List.of(
                        chamada(1L, 0, 100, 1, ResultadoDaChamada.SUCESSO, null, null, null, 0),
                        chamada(1L, 1, 100, 1, ResultadoDaChamada.SUCESSO, null, null, null, 1),
                        chamada(1L, 2, 100, 1, ResultadoDaChamada.SUCESSO, null, null, null, 2),
                        chamada(1L, 3, 100, 1, ResultadoDaChamada.LIMITE_DE_CHAMADAS, null, null, null, 4),
                        chamada(4L, 4, 10, 1, ResultadoDaChamada.SUCESSO, null, null, null, 0));

        List<MetricasDoProvedor> metricas = calcular.calcular(PROVEDORES, chamadas, List.of());

        MetricasDoProvedor gemini = metricas.get(0);
        assertThat(gemini.chamadas()).isEqualTo(4);
        assertThat(gemini.mediaDeChamadasHttp()).isEqualByComparingTo("2.3");
        assertThat(gemini.mediaDeChamadasHttp().scale()).isEqualTo(1);
        assertThat(gemini.maximoDeChamadasHttp()).isEqualTo(4);

        MetricasDoProvedor fake = metricas.get(1);
        assertThat(fake.mediaDeChamadasHttp()).isNull();
        assertThat(fake.maximoDeChamadasHttp()).isNull();
    }
}
