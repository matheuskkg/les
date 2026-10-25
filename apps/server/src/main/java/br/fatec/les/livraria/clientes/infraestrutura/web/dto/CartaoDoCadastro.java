package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import java.time.YearMonth;

/**
 * Cartão informado no cadastro e nas rotas {@code /api/me/cartoes} (RN-CLI-004, RN-CLI-005, DEC-06, DEC-18), sem
 * código de segurança (DEC-10) e sem a bandeira, que é identificada pelo número.
 */
public record CartaoDoCadastro(String numero, String nomeImpresso, YearMonth validade) {
}
