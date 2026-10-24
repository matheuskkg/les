package br.fatec.les.livraria.notificacoes.infraestrutura.web.dto;

import java.time.Instant;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

/**
 * Notificação no JSON da API e no {@code data} do evento SSE (DEC-13, DEC-07). O {@code id} é o id do evento no
 * stream; {@code recurso} e {@code recursoId}, quando presentes, montam o link para a tela do recurso.
 */
public record NotificacaoDto(
        Long id,
        TipoNotificacao tipo,
        String mensagem,
        @Nullable String recurso,
        @Nullable Long recursoId,
        Instant criadaEm,
        @Nullable Instant lidaEm) {
}
