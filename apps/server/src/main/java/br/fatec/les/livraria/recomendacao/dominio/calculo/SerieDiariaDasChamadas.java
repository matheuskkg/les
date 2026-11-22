package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadasNoDia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chamadas e falhas por dia para o gráfico das métricas (RNF-REC-001, DEC-03), somando todos os provedores. Todo dia do
 * período tem um ponto, com zero quando não houve chamada, para o eixo do gráfico ser contínuo.
 */
@Component
public class SerieDiariaDasChamadas {

    public List<ChamadasNoDia> calcular(PeriodoDasMetricas periodo, List<ChamadaIa> chamadas) {
        Map<LocalDate, long[]> porDia = new HashMap<>();
        for (ChamadaIa chamada : chamadas) {
            long[] contagem = porDia.computeIfAbsent(chamada.iniciadaEm().toLocalDate(), dia -> new long[2]);
            contagem[0]++;
            if (chamada.resultado() != ResultadoDaChamada.SUCESSO) {
                contagem[1]++;
            }
        }
        return periodo.dias().stream()
                .map(dia -> {
                    long[] contagem = porDia.getOrDefault(dia, new long[2]);
                    return new ChamadasNoDia(dia, contagem[0], contagem[1]);
                })
                .toList();
    }
}
