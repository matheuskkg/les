package br.fatec.les.livraria.compartilhado.aplicacao;

import java.util.Locale;
import java.util.Objects;

/**
 * Um critério de ordenação de uma listagem (DEC-07).
 *
 * @param campo campo de ordenação já traduzido para a propriedade do modelo
 * @param direcao {@code ASC} ou {@code DESC}
 */
public record Ordem(String campo, Direcao direcao) {

    public enum Direcao {
        ASC,
        DESC
    }

    public Ordem {
        if (campo == null || campo.isBlank()) {
            throw new ParametroInvalido("ordem", "Informe o campo da ordenação no formato campo,asc|desc.");
        }
        campo = campo.trim();
        Objects.requireNonNull(direcao, "direcao");
    }

    /** Lê um valor no formato {@code campo,asc|desc}, como recebido no query param {@code ordem}. */
    public static Ordem de(String valor) {
        String[] partes = valor == null ? new String[0] : valor.split(",", -1);
        if (partes.length != 2 || partes[0].isBlank() || partes[1].isBlank()) {
            throw new ParametroInvalido("ordem", "Ordenação inválida: use o formato campo,asc|desc.");
        }
        return new Ordem(partes[0], direcao(partes[1]));
    }

    private static Direcao direcao(String valor) {
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> Direcao.ASC;
            case "desc" -> Direcao.DESC;
            default -> throw new ParametroInvalido("ordem", "Direção de ordenação inválida: use asc ou desc.");
        };
    }
}
