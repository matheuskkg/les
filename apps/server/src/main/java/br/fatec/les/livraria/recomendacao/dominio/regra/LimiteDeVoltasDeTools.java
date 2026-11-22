package br.fatec.les.livraria.recomendacao.dominio.regra;

/**
 * Uma mensagem ao assistente pode executar no máximo {@value #MAXIMO_DE_VOLTAS} voltas de tools de leitura antes da
 * resposta final (RNF-REC-001, DEC-03). Passar do limite é falha do provedor, com o resultado {@code
 * LIMITE_DE_CHAMADAS}, e a cadeia segue para o próximo.
 */
public class LimiteDeVoltasDeTools {

    /** Voltas que executam tools de leitura antes da resposta final. */
    public static final int MAXIMO_DE_VOLTAS = 3;

    /** Indica se ainda cabe uma volta que executa tools de leitura. */
    public boolean permite(int voltasDeTools) {
        return voltasDeTools < MAXIMO_DE_VOLTAS;
    }
}
