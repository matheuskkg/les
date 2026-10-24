package br.fatec.les.livraria.analise.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * O período tem de 1 a {@value #MAXIMO_DE_MESES} meses, contando o inicial e o final (RN-ANA-002). O mínimo de 1 mês
 * é o de um período com o mesmo mês no início e no fim; um fim anterior ao início é tratado pelo
 * {@link FimNaoAnteriorAoInicio}.
 */
@Component
public class PeriodoDeAte24Meses implements Regra<SolicitacaoDeAnalise> {

    public static final int MAXIMO_DE_MESES = 24;

    @Override
    public void validar(SolicitacaoDeAnalise alvo, Violacoes violacoes) {
        if (alvo.quantidadeDeMeses() > MAXIMO_DE_MESES) {
            violacoes.adicionar("RN-ANA-002",
                    "O período da análise deve ter de 1 a %d meses.".formatted(MAXIMO_DE_MESES));
        }
    }
}
