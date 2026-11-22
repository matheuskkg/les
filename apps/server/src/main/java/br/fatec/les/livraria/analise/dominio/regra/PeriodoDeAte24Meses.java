package br.fatec.les.livraria.analise.dominio.regra;

import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * O período pode tocar de 1 a {@value #MAXIMO_DE_MESES} meses calendário, contando o mês do início e o do fim
 * (RN-ANA-002). Como as datas são exatas, um mesmo mês no início e no fim — ainda que em dias diferentes — é um mês. A
 * conta usa aritmética de meses, não de dias, para não variar com anos bissextos nem com o tamanho dos meses. Um fim
 * anterior ao início é tratado pelo {@link FimNaoAnteriorAoInicio}.
 */
@Component
public class PeriodoDeAte24Meses implements Regra<SolicitacaoDeAnalise> {

    public static final int MAXIMO_DE_MESES = 24;

    @Override
    public void validar(SolicitacaoDeAnalise alvo, Violacoes violacoes) {
        if (alvo.quantidadeDeMeses() > MAXIMO_DE_MESES) {
            violacoes.adicionar(
                    "RN-ANA-002", "O período da análise deve ter de 1 a %d meses.".formatted(MAXIMO_DE_MESES));
        }
    }
}
