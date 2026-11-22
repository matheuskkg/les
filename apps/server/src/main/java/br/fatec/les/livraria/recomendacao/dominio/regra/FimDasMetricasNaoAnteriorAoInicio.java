package br.fatec.les.livraria.recomendacao.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PeriodoDasMetricas;

import org.springframework.stereotype.Component;

/** O dia final das métricas da IA não pode ser anterior ao inicial (RNF-REC-001, DEC-03). */
@Component
public class FimDasMetricasNaoAnteriorAoInicio implements Regra<PeriodoDasMetricas> {

    @Override
    public void validar(PeriodoDasMetricas alvo, Violacoes violacoes) {
        if (alvo.fim().isBefore(alvo.inicio())) {
            violacoes.adicionar("RNF-REC-001", "O dia final não pode ser anterior ao inicial.");
        }
    }
}
