package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadasNoDia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MetricasDaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MetricasDoProvedor;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;

/**
 * Métricas de funcionamento da IA no período (RNF-REC-001, DEC-03): uma linha por provedor, na ordem da cadeia, as
 * recomendações gravadas sem IA, que não têm provedor, e a série diária do gráfico com todos os dias do período.
 * As taxas vão de 0 a 1 e são omitidas, como as latências e a média e o máximo das chamadas HTTP, quando não há o que
 * medir.
 */
public record MetricasDaIaDto(
        LocalDate inicio,
        LocalDate fim,
        List<MetricasDoProvedorDto> provedores,
        long recomendacoesSemIa,
        List<ChamadasNoDiaDto> serie) {

    public static MetricasDaIaDto de(MetricasDaIa metricas) {
        return new MetricasDaIaDto(
                metricas.periodo().inicio(),
                metricas.periodo().fim(),
                metricas.provedores().stream().map(MetricasDoProvedorDto::de).toList(),
                metricas.recomendacoesSemIa(),
                metricas.serie().stream().map(ChamadasNoDiaDto::de).toList());
    }

    /** Métricas de um provedor no período. */
    public record MetricasDoProvedorDto(
            Long id,
            String nome,
            long chamadas,
            long sucessos,
            @Nullable Double taxaDeSucesso,
            List<FalhasPorResultadoDto> falhasPorResultado,
            @Nullable UltimaFalhaDto ultimaFalha,
            @Nullable Integer latenciaMediaMs,
            @Nullable Integer latenciaP95Ms,
            long tokensEntrada,
            long tokensSaida,
            @Nullable BigDecimal mediaDeChamadasHttp,
            @Nullable Integer maximoDeChamadasHttp,
            long atendimentosPorReserva,
            long avaliadas,
            long positivas,
            @Nullable Double taxaDeAprovacao) {

        static MetricasDoProvedorDto de(MetricasDoProvedor metricas) {
            return new MetricasDoProvedorDto(
                    metricas.provedorIaId(),
                    metricas.nome(),
                    metricas.chamadas(),
                    metricas.sucessos(),
                    metricas.taxaDeSucesso(),
                    falhas(metricas.falhasPorResultado()),
                    UltimaFalhaDto.de(metricas.ultimaFalha()),
                    metricas.latenciaMediaMs(),
                    metricas.latenciaP95Ms(),
                    metricas.tokensEntrada(),
                    metricas.tokensSaida(),
                    metricas.mediaDeChamadasHttp(),
                    metricas.maximoDeChamadasHttp(),
                    metricas.atendimentosPorReserva(),
                    metricas.avaliadas(),
                    metricas.positivas(),
                    metricas.taxaDeAprovacao());
        }

        /** Na ordem do enum, só os resultados que ocorreram. */
        private static List<FalhasPorResultadoDto> falhas(Map<ResultadoDaChamada, Long> porResultado) {
            return Arrays.stream(ResultadoDaChamada.values())
                    .filter(porResultado::containsKey)
                    .map(resultado -> new FalhasPorResultadoDto(resultado, porResultado.get(resultado)))
                    .toList();
        }
    }

    /** Quantidade de falhas com um resultado. */
    public record FalhasPorResultadoDto(ResultadoDaChamada resultado, long quantidade) {
    }

    /** Chamadas e falhas de todos os provedores em um dia. */
    public record ChamadasNoDiaDto(LocalDate dia, long chamadas, long falhas) {

        static ChamadasNoDiaDto de(ChamadasNoDia dia) {
            return new ChamadasNoDiaDto(dia.dia(), dia.chamadas(), dia.falhas());
        }
    }
}
