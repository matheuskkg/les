package br.fatec.les.livraria.clientes.dominio.ranking;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/** Nota de 0 a 100 da soma dos valores das compras na janela, por faixa com limites inclusivos (RN-CLI-007, DEC-04). */
public final class NotaDeValor {

    private static final Dinheiro ATE_100 = Dinheiro.de("100.00");
    private static final Dinheiro ATE_300 = Dinheiro.de("300.00");
    private static final Dinheiro ATE_600 = Dinheiro.de("600.00");
    private static final Dinheiro ATE_1000 = Dinheiro.de("1000.00");

    private NotaDeValor() {
    }

    public static int de(Dinheiro soma) {
        if (!soma.ehPositivo()) {
            return 0;
        }
        if (!soma.maiorQue(ATE_100)) {
            return 20;
        }
        if (!soma.maiorQue(ATE_300)) {
            return 40;
        }
        if (!soma.maiorQue(ATE_600)) {
            return 60;
        }
        return soma.maiorQue(ATE_1000) ? 100 : 80;
    }
}
