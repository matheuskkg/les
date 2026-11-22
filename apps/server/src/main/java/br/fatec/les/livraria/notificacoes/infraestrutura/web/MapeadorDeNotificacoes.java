package br.fatec.les.livraria.notificacoes.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.infraestrutura.web.dto.NotificacaoDto;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Converte a notificação de domínio no DTO da listagem e do evento SSE (DEC-07, DEC-13). A API expõe data e hora em
 * UTC, como {@link Instant}; o banco guarda a hora local no fuso do {@link RelogioDoSistema}.
 */
@Component
public class MapeadorDeNotificacoes {

    public NotificacaoDto paraDto(Notificacao notificacao) {
        return new NotificacaoDto(
                notificacao.getId(),
                notificacao.getTipo(),
                notificacao.getMensagem(),
                notificacao.getRecurso(),
                notificacao.getRecursoId(),
                paraInstant(notificacao.getCriadaEm()),
                paraInstant(notificacao.getLidaEm()));
    }

    private static Instant paraInstant(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.atZone(RelogioDoSistema.FUSO).toInstant();
    }
}
