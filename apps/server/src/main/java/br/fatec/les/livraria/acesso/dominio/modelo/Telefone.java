package br.fatec.les.livraria.acesso.dominio.modelo;

import java.util.Objects;

/**
 * Telefone do usuário, guardado como DDD e número (RN-CLI-006, DEC-10). O login compara só os dígitos, sem o tipo. Os
 * helpers de dígitos também são usados pelas regras de cadastro dos módulos que recebem telefone, CEP ou número de
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

    /** Exibição {@code (11) 98888-7001} ou {@code (11) 3333-4444}. */
    public String formatado() {
        int corte = numero.length() - 4;
        return "(" + ddd + ") " + numero.substring(0, corte) + "-" + numero.substring(corte);
    }

    /**
     * Dígitos de um telefone digitado, sem o código do país quando informado: {@code +55 (11) 98888-7777} vira {@code
     * 11988887777}.
     */
    public static String digitosNacionais(String valor) {
        String digitos = somenteDigitos(valor);
        boolean comPais = (digitos.length() == 12 || digitos.length() == 13) && digitos.startsWith("55");
        return comPais ? digitos.substring(2) : digitos;
    }

    /** DDD + número com 10 ou 11 dígitos, já sem máscara. */
    public static boolean digitosValidos(String digitos) {
        return digitos != null && (digitos.length() == 10 || digitos.length() == 11);
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
