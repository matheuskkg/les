package br.fatec.les.livraria.acesso.dominio.modelo;

import java.util.Objects;

/**
 * Telefone do usuário, guardado como DDD e número (RN-CLI-006, DEC-10). O login compara só os dígitos, sem o tipo.
 * Os helpers de dígitos também são usados pelas regras de cadastro dos módulos que recebem telefone, CEP ou número de
 * cartão com máscara.
 */
public record Telefone(TipoTelefone tipo, String ddd, String numero) {

    public Telefone {
        Objects.requireNonNull(tipo, "tipo");
        if (ddd == null || !ddd.matches("\\d{2}")) {
            throw new IllegalArgumentException("DDD deve ter 2 dígitos: " + ddd);
        }
        if (numero == null || !numero.matches("\\d{8,9}")) {
            throw new IllegalArgumentException("Número de telefone deve ter 8 ou 9 dígitos: " + numero);
        }
    }

    /** DDD + número, usados na comparação do login e da unicidade. */
    public String digitos() {
        return ddd + numero;
    }

    /** Remove tudo que não for dígito; um valor nulo vira texto vazio. */
    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    /** DDD válido: exatamente 2 dígitos, com ou sem máscara. */
    public static boolean dddValido(String ddd) {
        return ddd != null && somenteDigitos(ddd).length() == 2;
    }

    /** Número de telefone válido: 8 ou 9 dígitos, com ou sem máscara. */
    public static boolean numeroValido(String numero) {
        if (numero == null) {
            return false;
        }
        int digitos = somenteDigitos(numero).length();
        return digitos == 8 || digitos == 9;
    }

    /** Telefone completo válido: DDD e número dentro do formato (RN-CLI-006). */
    public static boolean valido(String ddd, String numero) {
        return dddValido(ddd) && numeroValido(numero);
    }
}
