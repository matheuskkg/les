package br.fatec.les.livraria.recomendacao.dominio.regra;

/**
 * Uma mensagem ao assistente pode pedir no máximo {@value #MAXIMO_DE_CORRECOES} correção de formato ao mesmo provedor
 * (RNF-REC-001, DEC-03). Passar do limite é falha do provedor, com o resultado {@code RESPOSTA_INVALIDA}, e a cadeia
 * segue para o próximo.
 */
public class LimiteDeCorrecoesDeFormato {

    /** Reenvios da resposta inválida ao mesmo provedor, pedindo o formato certo. */
    public static final int MAXIMO_DE_CORRECOES = 1;

    /** Indica se ainda cabe uma correção de formato. */
    public boolean permite(int correcoes) {
        return correcoes < MAXIMO_DE_CORRECOES;
    }
}
