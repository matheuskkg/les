package br.fatec.les.livraria.compartilhado.dominio;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Endereço de e-mail com formato válido e no máximo 254 caracteres, guardado em minúsculas e sem espaços nas pontas.
 */
public record Email(String endereco) {

    /** Tamanho máximo de um endereço de e-mail (RFC 5321). */
    public static final int TAMANHO_MAXIMO = 254;

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (!ehValido(endereco)) {
            throw new IllegalArgumentException("E-mail inválido: " + endereco);
        }
        endereco = endereco.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean ehValido(String endereco) {
        if (endereco == null) {
            return false;
        }
        String normalizado = endereco.strip();
        return normalizado.length() <= TAMANHO_MAXIMO && FORMATO.matcher(normalizado).matches();
    }
}
