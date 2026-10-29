package br.fatec.les.livraria.recomendacao.dominio.regra;

/**
 * Uma mensagem ao assistente pode fazer no máximo {@code max-chamadas} chamadas HTTP ao mesmo provedor, contando as
 * voltas de tools e a correção de formato (RNF-REC-001, DEC-03). Passar do limite é falha do provedor, com o
 * resultado {@code LIMITE_DE_CHAMADAS}, e a cadeia segue para o próximo.
 */
public class LimiteDeChamadasHttp {

    /** Indica se ainda cabe uma chamada HTTP dentro de {@code max-chamadas}. */
    public boolean permite(int chamadasHttp, int maxChamadas) {
        return chamadasHttp < maxChamadas;
    }
}
