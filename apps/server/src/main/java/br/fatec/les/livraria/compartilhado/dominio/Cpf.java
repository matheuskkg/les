package br.fatec.les.livraria.compartilhado.dominio;

/** CPF válido, guardado só com os 11 dígitos. */
public record Cpf(String numero) {

    public Cpf {
        if (!ehValido(numero)) {
            throw new IllegalArgumentException("CPF inválido: " + numero);
        }
        numero = somenteDigitos(numero);
    }

    /** Aceita o CPF com ou sem máscara ({@code 123.456.789-09} ou {@code 12345678909}). */
    public static boolean ehValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}")) {
            return false;
        }
        String digitos = somenteDigitos(cpf);
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    public String formatado() {
        return numero.substring(0, 3) + "." + numero.substring(3, 6) + "." + numero.substring(6, 9) + "-"
                + numero.substring(9);
    }

    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static String somenteDigitos(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
