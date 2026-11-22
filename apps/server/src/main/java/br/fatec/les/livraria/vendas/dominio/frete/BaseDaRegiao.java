package br.fatec.les.livraria.vendas.dominio.frete;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Valor base do frete pela região do CEP de entrega (RF-VEN-004, DEC-05.3), pelo primeiro dígito: 0 e 1 (SP) R$ 12,00;
 * 2 e 3 (RJ, ES, MG) R$ 15,00; 8 e 9 (Sul) R$ 18,00; 7 (Centro-Oeste, TO e RO) R$ 22,00; 4 e 5 (BA, SE, PE, AL, PB, RN)
 * R$ 25,00; 6 (CE, PI, MA e Norte) R$ 30,00.
 */
public final class BaseDaRegiao {

    private BaseDaRegiao() {}

    /**
     * @param cep CEP de 8 dígitos, com ou sem máscara
     * @throws IllegalArgumentException se o CEP não começar por um dígito
     */
    public static Dinheiro de(String cep) {
        String digitos = cep.replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            throw new IllegalArgumentException("CEP sem dígitos.");
        }
        return switch (digitos.charAt(0)) {
            case '0', '1' -> Dinheiro.de("12.00");
            case '2', '3' -> Dinheiro.de("15.00");
            case '8', '9' -> Dinheiro.de("18.00");
            case '7' -> Dinheiro.de("22.00");
            case '4', '5' -> Dinheiro.de("25.00");
            default -> Dinheiro.de("30.00");
        };
    }
}
