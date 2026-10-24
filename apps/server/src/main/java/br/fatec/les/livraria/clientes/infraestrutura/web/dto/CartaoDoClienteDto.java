package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import java.time.YearMonth;

/**
 * Cartão no detalhe do cliente (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10). O número sai mascarado e o código de
 * segurança nunca é gravado.
 */
public record CartaoDoClienteDto(
        Long id,
        String numero,
        String nomeImpresso,
        BandeiraDoCartaoDto bandeira,
        YearMonth validade,
        boolean preferencial) {
}
