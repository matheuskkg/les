package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.YearMonth;

/**
 * Cartão informado no cadastro do cliente (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10, DEC-18), sem o código de segurança.
 * A bandeira é identificada pelo número, e o primeiro cartão da lista vira o preferencial.
 */
public record NovoCartao(String numero, String nomeImpresso, YearMonth validade) {}
