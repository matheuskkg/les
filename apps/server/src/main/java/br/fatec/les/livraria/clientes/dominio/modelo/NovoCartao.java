package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.YearMonth;

/**
 * Cartão informado no cadastro do cliente (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10), sem o código de segurança. O
 * primeiro cartão da lista vira o preferencial.
 */
public record NovoCartao(
        String numero,
        String nomeImpresso,
        Long bandeiraId,
        YearMonth validade) {
}
