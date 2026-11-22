package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;

/**
 * Comando de {@code PUT /api/me/cartoes/{id}} (RF-CLI-007): o id do cartão e os dados novos. O código de segurança não
 * faz parte do comando: nunca é gravado (DEC-10).
 */
public record AlteracaoDeCartao(Long id, NovoCartao dados) {}
