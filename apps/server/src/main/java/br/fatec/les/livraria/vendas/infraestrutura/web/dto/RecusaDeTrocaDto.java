package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import org.jspecify.annotations.Nullable;

/**
 * Recusa de uma troca pelo admin (DEC-12). Motivo ausente chega à regra, que responde 422.
 *
 * @param motivo motivo enviado ao cliente na notificação
 */
public record RecusaDeTrocaDto(@Nullable String motivo) {}
