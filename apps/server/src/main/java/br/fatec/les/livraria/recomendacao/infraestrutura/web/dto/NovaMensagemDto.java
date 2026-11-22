package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Mensagem do cliente ao assistente, com até 500 caracteres (RNF-REC-001, DEC-03). */
public record NovaMensagemDto(@Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500) String texto) {}
