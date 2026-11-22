package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeCartao;

import java.math.BigDecimal;

/**
 * Cartão do pagamento e o valor que ele paga (RF-VEN-006, RN-VEN-004, DEC-11). O valor fica como informado, para que as
 * regras recusem centavos fracionados.
 */
public record CartaoInformado(EscolhaDeCartao escolha, BigDecimal valor) {}
