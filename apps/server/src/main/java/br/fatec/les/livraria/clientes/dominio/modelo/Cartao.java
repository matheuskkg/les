package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Cartão de crédito do cliente (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10). O código de segurança não é gravado: é
 * pedido a cada compra. O número é guardado completo para o cadastro, mas a API só devolve a versão mascarada.
 */
public record Cartao(
        Long id,
        String numero,
        String nomeImpresso,
        Bandeira bandeira,
        YearMonth validade,
        boolean preferencial) {

    public Cartao {
        Objects.requireNonNull(numero, "numero");
        Objects.requireNonNull(nomeImpresso, "nomeImpresso");
        Objects.requireNonNull(bandeira, "bandeira");
        Objects.requireNonNull(validade, "validade");
    }

    /** Número como a API o devolve (DEC-10): {@code **** **** **** 1234}. */
    public String numeroMascarado() {
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }
}
