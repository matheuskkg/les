package br.fatec.les.livraria.clientes.dominio.modelo;

import java.util.Objects;

/**
 * Prefixo de número de cartão de crédito de uma bandeira (RN-CLI-005, DEC-18): um prefixo puro ({@code 4}) ou uma
 * faixa inclusiva ({@code 51-55}, {@code 2221-2720}). O texto vem da coluna {@code prefixos} da tabela de bandeiras do
 * seed e é comparado com o início do número.
 */
public record PrefixoDeCartao(String inicio, String fim) {

    public PrefixoDeCartao {
        exigirDigitos(inicio, "início");
        exigirDigitos(fim, "fim");
        if (inicio.length() != fim.length()) {
            throw new IllegalArgumentException("O início e o fim da faixa devem ter o mesmo tamanho: " + inicio
                    + "-" + fim);
        }
        if (inicio.compareTo(fim) > 0) {
            throw new IllegalArgumentException("A faixa de prefixos está invertida: " + inicio + "-" + fim);
        }
    }

    /** Interpreta um item da coluna {@code prefixos}: {@code 4} ou {@code 51-55}. */
    public static PrefixoDeCartao de(String texto) {
        Objects.requireNonNull(texto, "texto");
        String[] partes = texto.strip().split("-", -1);
        if (partes.length == 1) {
            return new PrefixoDeCartao(partes[0], partes[0]);
        }
        if (partes.length == 2) {
            return new PrefixoDeCartao(partes[0], partes[1]);
        }
        throw new IllegalArgumentException("Prefixo de cartão inválido: " + texto);
    }

    /** {@code true} se o início do número casa com o prefixo ou cai dentro da faixa inclusiva. */
    public boolean casaCom(String numero) {
        if (numero == null || numero.length() < inicio.length()) {
            return false;
        }
        String trecho = numero.substring(0, inicio.length());
        return trecho.compareTo(inicio) >= 0 && trecho.compareTo(fim) <= 0;
    }

    /** Quantidade de dígitos do prefixo, usada para escolher o mais longo. */
    public int tamanho() {
        return inicio.length();
    }

    /** Representação da coluna {@code prefixos}: prefixo puro ou faixa {@code inicio-fim}. */
    public String texto() {
        return inicio.equals(fim) ? inicio : inicio + "-" + fim;
    }

    private static void exigirDigitos(String valor, String nome) {
        if (valor == null || !valor.matches("\\d+")) {
            throw new IllegalArgumentException("O " + nome + " do prefixo deve ter só dígitos: " + valor);
        }
    }
}
