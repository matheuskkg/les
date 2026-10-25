package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import io.swagger.v3.oas.annotations.media.Schema;

/** Avaliação de um item recomendado ou de uma resposta do assistente (RNF-REC-001, DEC-03). */
public record AvaliacaoDto(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) Avaliacao avaliacao) {
}
