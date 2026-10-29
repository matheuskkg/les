package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.math.BigDecimal;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Métricas de um provedor no período (RNF-REC-001, DEC-03). As taxas vão de 0 a 1 e, como as latências, são nulas
 * quando não há o que medir.
 *
 * @param provedorIaId           id em {@code provedor_ia}
 * @param nome                   nome do provedor
 * @param chamadas               tentativas no período
 * @param sucessos               tentativas com {@link ResultadoDaChamada#SUCESSO}
 * @param taxaDeSucesso          sucessos sobre chamadas
 * @param falhasPorResultado     quantidade de falhas por resultado, só os que ocorreram
 * @param ultimaFalha            última falha do período
 * @param latenciaMediaMs        latência média dos sucessos
 * @param latenciaP95Ms          percentil 95 da latência dos sucessos
 * @param tokensEntrada          soma dos tokens de entrada informados
 * @param tokensSaida            soma dos tokens de saída informados
 * @param mediaDeChamadasHttp    média de chamadas HTTP por mensagem, só nas tentativas com ao menos uma chamada
 * @param maximoDeChamadasHttp   maior número de chamadas HTTP de uma mensagem, nas mesmas tentativas
 * @param atendimentosPorReserva sucessos com tentativa maior que 1
 * @param avaliadas              recomendações e respostas avaliadas, geradas por chamadas do período
 * @param positivas              avaliações positivas entre as avaliadas
 * @param taxaDeAprovacao        positivas sobre avaliadas
 */
public record MetricasDoProvedor(
        Long provedorIaId,
        String nome,
        long chamadas,
        long sucessos,
        @Nullable Double taxaDeSucesso,
        Map<ResultadoDaChamada, Long> falhasPorResultado,
        @Nullable UltimaFalha ultimaFalha,
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

    public MetricasDoProvedor {
        falhasPorResultado = Map.copyOf(falhasPorResultado);
    }
}
