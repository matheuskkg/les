package br.fatec.les.livraria.notificacoes.infraestrutura.web;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.infraestrutura.web.dto.NotificacaoDto;

/**
 * Converte a notificação de domínio no DTO da listagem e do evento SSE (DEC-07, DEC-13). A API expõe data e hora em
 * UTC, como {@link Instant}; o banco guarda a hora local do servidor.
 */
@Component
public class MapeadorDeNotificacoes {

    private static final ZoneId FUSO = ZoneId.systemDefault();

    public NotificacaoDto paraDto(Notificacao notificacao) {
        return new NotificacaoDto(notificacao.getId(), notificacao.getTipo(), notificacao.getMensagem(),
                notificacao.getRecurso(), notificacao.getRecursoId(), paraInstant(notificacao.getCriadaEm()),
                paraInstant(notificacao.getLidaEm()));
    }

    private static Instant paraInstant(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.atZone(FUSO).toInstant();
    }
}
