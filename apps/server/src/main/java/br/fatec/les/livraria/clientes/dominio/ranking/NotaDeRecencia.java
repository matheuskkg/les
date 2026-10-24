package br.fatec.les.livraria.clientes.dominio.ranking;

/** Nota de 0 a 100 dos dias desde a última compra (RN-CLI-007, DEC-04); sem compra na janela, 0. */
public final class NotaDeRecencia {

    private NotaDeRecencia() {
    }

    /** @param dias dias corridos entre a data da última compra e a data do cálculo; negativo não ocorre */
    public static int de(long dias) {
        if (dias <= 30) {
            return 100;
        }
        if (dias <= 90) {
            return 80;
        }
        if (dias <= 180) {
            return 60;
        }
        if (dias <= 270) {
            return 40;
        }
        return dias <= Ranking.JANELA_EM_DIAS ? 20 : 0;
    }
}
