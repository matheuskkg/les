package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Nova cadeia de provedores (RNF-REC-001, DEC-03): todos os provedores, na ordem desejada, com a ativação de cada um.
 */
public record AlteracaoDaCadeiaDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<PosicaoNaCadeiaDto> provedores) {

    /** Provedor na posição da lista; a ordem gravada é a posição, a partir de 1. */
    public record PosicaoNaCadeiaDto(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Long id,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean ativo) {}
}
