package br.fatec.les.livraria.acesso.dominio.modelo;

import java.util.Objects;

/**
 * Telefone do usuário, guardado como DDD e número (RN-CLI-006, DEC-10). O login compara só os dígitos, sem o tipo.
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
}
