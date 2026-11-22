package br.fatec.les.livraria.analise.dominio.regra;

import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/** A data final da análise não pode ser anterior à inicial (RF-ANA-002). */
@Component
public class FimNaoAnteriorAoInicio implements Regra<SolicitacaoDeAnalise> {

    @Override
    public void validar(SolicitacaoDeAnalise alvo, Violacoes violacoes) {
        if (alvo.fim().isBefore(alvo.inicio())) {
            violacoes.adicionar("RF-ANA-002", "A data final não pode ser anterior à inicial.");
        }
    }
}
