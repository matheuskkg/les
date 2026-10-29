package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDeChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MetricasDoProvedor;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;
import lombok.RequiredArgsConstructor;

/**
 * Métricas de cada provedor sobre as chamadas do período (RNF-REC-001, DEC-03): chamadas e taxa de sucesso, falhas
 * por resultado e a última, latência média e p95 dos sucessos, tokens, chamadas HTTP por mensagem (média e máximo),
 * atendimentos por reserva (sucesso com tentativa maior que 1) e taxa de aprovação. A aprovação considera as
 * avaliações das recomendações e respostas geradas pelas chamadas do período, atribuídas ao provedor da chamada. A
 * média e o máximo das chamadas HTTP ignoram as tentativas sem chamada (o provedor fake e os registros antigos).
 */
@Component
@RequiredArgsConstructor
public class CalcularMetricasDosProvedores {

    private final Percentil95 percentil95;

    /**
     * @param provedores provedores na ordem da cadeia; todos aparecem, mesmo sem chamadas
     * @param chamadas   tentativas do período, de todos os provedores
     * @param avaliacoes avaliações das recomendações e respostas geradas pelas chamadas do período
     */
    public List<MetricasDoProvedor> calcular(
            List<ProvedorIa> provedores, List<ChamadaIa> chamadas, List<AvaliacaoDeChamada> avaliacoes) {
        Map<Long, List<ChamadaIa>> chamadasPorProvedor = chamadas.stream()
                .collect(Collectors.groupingBy(ChamadaIa::provedorIaId));
        Map<Long, List<AvaliacaoDeChamada>> avaliacoesPorProvedor = avaliacoes.stream()
                .collect(Collectors.groupingBy(AvaliacaoDeChamada::provedorIaId));
        return provedores.stream()
                .map(provedor -> doProvedor(provedor,
                        chamadasPorProvedor.getOrDefault(provedor.id(), List.of()),
                        avaliacoesPorProvedor.getOrDefault(provedor.id(), List.of())))
                .toList();
    }

    private MetricasDoProvedor doProvedor(
            ProvedorIa provedor, List<ChamadaIa> chamadas, List<AvaliacaoDeChamada> avaliacoes) {
        List<ChamadaIa> sucessos = chamadas.stream()
                .filter(chamada -> chamada.resultado() == ResultadoDaChamada.SUCESSO)
                .toList();
        List<ChamadaIa> falhas = chamadas.stream()
                .filter(chamada -> chamada.resultado() != ResultadoDaChamada.SUCESSO)
                .toList();
        Map<ResultadoDaChamada, Long> falhasPorResultado = new EnumMap<>(ResultadoDaChamada.class);
        falhas.forEach(falha -> falhasPorResultado.merge(falha.resultado(), 1L, Long::sum));
        UltimaFalha ultimaFalha = falhas.stream()
                .max(Comparator.comparing(ChamadaIa::iniciadaEm))
                .map(UltimaFalha::de)
                .orElse(null);
        List<Integer> latencias = sucessos.stream().map(ChamadaIa::latenciaMs).toList();
        Integer latenciaMedia = latencias.isEmpty() ? null
                : (int) Math.round(latencias.stream().mapToInt(Integer::intValue).average().orElseThrow());
        long positivas = avaliacoes.stream().filter(avaliacao -> avaliacao.avaliacao() == Avaliacao.POSITIVA).count();
        List<Integer> chamadasHttp = chamadas.stream()
                .map(ChamadaIa::chamadasHttp)
                .filter(quantidade -> quantidade > 0)
                .toList();

        return new MetricasDoProvedor(
                provedor.id(),
                provedor.nome(),
                chamadas.size(),
                sucessos.size(),
                taxa(sucessos.size(), chamadas.size()),
                falhasPorResultado,
                ultimaFalha,
                latenciaMedia,
                percentil95.calcular(latencias),
                soma(chamadas, ChamadaIa::tokensEntrada),
                soma(chamadas, ChamadaIa::tokensSaida),
                media(chamadasHttp),
                chamadasHttp.stream().max(Integer::compareTo).orElse(null),
                sucessos.stream().filter(sucesso -> sucesso.tentativa() > 1).count(),
                avaliacoes.size(),
                positivas,
                taxa(positivas, avaliacoes.size()));
    }

    private static Double taxa(long parte, long total) {
        return total == 0 ? null : (double) parte / total;
    }

    /** Média com uma casa decimal; nula sem amostra. */
    private static BigDecimal media(List<Integer> amostras) {
        return amostras.isEmpty() ? null
                : BigDecimal.valueOf(amostras.stream().mapToInt(Integer::intValue).average().orElseThrow())
                        .setScale(1, RoundingMode.HALF_UP);
    }

    private static long soma(List<ChamadaIa> chamadas, Function<ChamadaIa, Integer> tokens) {
        return chamadas.stream().map(tokens).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
    }
}
