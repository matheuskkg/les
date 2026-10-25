package br.fatec.les.livraria.recomendacao.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;

/**
 * O período das métricas da IA tem no máximo {@value #MAXIMO_DE_DIAS} dias, contando o inicial e o final
 * (RNF-REC-001, DEC-03), para o gráfico diário continuar legível. O fim anterior ao início é tratado pela
 * {@link FimDasMetricasNaoAnteriorAoInicio}.
 */
@Component
public class PeriodoDasMetricasDeAte366Dias implements Regra<PeriodoDasMetricas> {

    public static final int MAXIMO_DE_DIAS = 366;

    @Override
    public void validar(PeriodoDasMetricas alvo, Violacoes violacoes) {
        if (alvo.quantidadeDeDias() > MAXIMO_DE_DIAS) {
            violacoes.adicionar("RNF-REC-001",
                    "O período das métricas pode ter até %d dias.".formatted(MAXIMO_DE_DIAS));
        }
    }
}
