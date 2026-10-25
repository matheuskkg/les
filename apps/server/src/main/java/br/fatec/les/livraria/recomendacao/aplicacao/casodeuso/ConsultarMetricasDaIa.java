package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.MetricasIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.calculo.CalcularMetricasDosProvedores;
import br.fatec.les.livraria.recomendacao.dominio.calculo.SerieDiariaDasChamadas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MetricasDaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;
import br.fatec.les.livraria.recomendacao.dominio.regra.FimDasMetricasNaoAnteriorAoInicio;
import br.fatec.les.livraria.recomendacao.dominio.regra.PeriodoDasMetricasDeAte366Dias;
import lombok.RequiredArgsConstructor;

/**
 * Métricas de funcionamento da IA no período, calculadas sobre {@code chamada_ia} (RNF-REC-001, DEC-03): uma linha
 * por provedor, as recomendações gravadas sem IA e a série diária de chamadas e falhas do gráfico.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMetricasDaIa {

    private final FimDasMetricasNaoAnteriorAoInicio fimNaoAnteriorAoInicio;
    private final PeriodoDasMetricasDeAte366Dias periodoDeAte366Dias;
    private final ProvedoresIaRepositorio provedores;
    private final MetricasIaRepositorio metricas;
    private final CalcularMetricasDosProvedores calcularMetricas;
    private final SerieDiariaDasChamadas serieDiaria;

    /**
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se o período for invertido ou maior que
     *         366 dias (HTTP 422)
     */
    @Transactional(readOnly = true)
    public MetricasDaIa executar(PeriodoDasMetricas periodo) {
        Violacoes.verificar(periodo, fimNaoAnteriorAoInicio, periodoDeAte366Dias);
        LocalDateTime de = periodo.primeiroInstante();
        LocalDateTime ate = periodo.fimExclusivo();
        List<ChamadaIa> chamadas = metricas.chamadas(de, ate);
        return new MetricasDaIa(
                periodo,
                calcularMetricas.calcular(provedores.listarEmOrdem(), chamadas, metricas.avaliacoes(de, ate)),
                metricas.recomendacoesSemIa(de, ate),
                serieDiaria.calcular(periodo, chamadas));
    }
}
