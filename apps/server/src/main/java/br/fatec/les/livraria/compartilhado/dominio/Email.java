package br.fatec.les.livraria.compartilhado.dominio;

import java.util.Locale;
import java.util.regex.Pattern;

/** Endereço de e-mail com formato válido, guardado em minúsculas e sem espaços nas pontas. */
public record Email(String endereco) {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (!ehValido(endereco)) {
            throw new IllegalArgumentException("E-mail inválido: " + endereco);
        }
        endereco = endereco.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean ehValido(String endereco) {
        return endereco != null && FORMATO.matcher(endereco.strip()).matches();
    }
}
