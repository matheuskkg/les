package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.YearMonth;
import java.util.Objects;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;

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

    /**
     * Monta o cartão a partir dos dados informados (RN-CLI-004, DEC-06, DEC-10), guardando só os dígitos do número e
     * aparando o nome impresso.
     */
    public static Cartao de(NovoCartao dados, Bandeira bandeira, Long id, boolean preferencial) {
        return new Cartao(id, Telefone.somenteDigitos(dados.numero()), dados.nomeImpresso().strip(), bandeira,
                dados.validade(), preferencial);
    }

    /** Número como a API o devolve (DEC-10): {@code **** **** **** 1234}. */
    public String numeroMascarado() {
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }
}
