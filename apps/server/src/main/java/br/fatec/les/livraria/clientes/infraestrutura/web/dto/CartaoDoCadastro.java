package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import java.time.YearMonth;

/** Cartão do cadastro (RN-CLI-004, RN-CLI-005, DEC-06), sem código de segurança (DEC-10). */
public record CartaoDoCadastro(String numero, String nomeImpresso, Long bandeiraId, YearMonth validade) {
}
