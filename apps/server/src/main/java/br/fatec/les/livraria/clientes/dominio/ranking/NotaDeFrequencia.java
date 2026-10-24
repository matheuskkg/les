package br.fatec.les.livraria.clientes.dominio.ranking;

/** Nota de 0 a 100 do número de compras na janela (RN-CLI-007, DEC-04). */
public final class NotaDeFrequencia {

    private NotaDeFrequencia() {
    }

    public static int de(int compras) {
        if (compras <= 0) {
            return 0;
        }
        if (compras == 1) {
            return 20;
        }
        if (compras == 2) {
            return 40;
        }
        if (compras <= 4) {
            return 60;
        }
        return compras <= 7 ? 80 : 100;
    }
}
