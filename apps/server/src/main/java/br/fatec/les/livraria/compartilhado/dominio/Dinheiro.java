package br.fatec.les.livraria.compartilhado.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Valor monetário em reais, sempre com 2 casas decimais e arredondamento {@code HALF_EVEN}. */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    private static final int ESCALA = 2;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "valor");
        valor = valor.setScale(ESCALA, ARREDONDAMENTO);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        return new Dinheiro(valor.subtract(outro.valor));
    }

    /** Multiplica sem arredondar o fator; só o resultado é arredondado. */
    public Dinheiro multiplicar(BigDecimal fator) {
        return new Dinheiro(valor.multiply(fator));
    }

    public Dinheiro multiplicar(int quantidade) {
        return multiplicar(BigDecimal.valueOf(quantidade));
    }

    public boolean ehZero() {
        return valor.signum() == 0;
    }

    public boolean ehPositivo() {
        return valor.signum() > 0;
    }

    public boolean ehNegativo() {
        return valor.signum() < 0;
    }

    public boolean maiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    public boolean menorQue(Dinheiro outro) {
        return compareTo(outro) < 0;
    }

    public static Dinheiro maior(Dinheiro a, Dinheiro b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static Dinheiro menor(Dinheiro a, Dinheiro b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
