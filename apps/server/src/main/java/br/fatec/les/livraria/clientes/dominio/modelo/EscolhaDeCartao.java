package br.fatec.les.livraria.clientes.dominio.modelo;

/**
 * Cartão escolhido numa compra (RF-VEN-006, DEC-10): um dos cadastrados, pelo {@code cartaoId}, ou um novo, informado
 * por inteiro. Incorporar o novo ao perfil é um cadastro de cartão à parte (RF-CLI-007).
 */
public record EscolhaDeCartao(Long cartaoId, NovoCartao novo) {}
