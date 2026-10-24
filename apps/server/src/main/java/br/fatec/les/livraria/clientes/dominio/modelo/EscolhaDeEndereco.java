package br.fatec.les.livraria.clientes.dominio.modelo;

/**
 * Endereço escolhido numa compra (RF-VEN-005, DEC-10): um dos cadastrados, pelo {@code enderecoId}, ou um novo,
 * informado por inteiro. Incorporar o novo ao perfil é um cadastro de endereço à parte (RF-CLI-006).
 */
public record EscolhaDeEndereco(Long enderecoId, NovoEndereco novo) {
}
