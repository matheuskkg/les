package br.fatec.les.livraria.notificacoes.infraestrutura.sse;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.CanaisDeEventos;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.infraestrutura.web.MapeadorDeNotificacoes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Canais SSE das notificações (DEC-13). Cada evento leva o id da notificação em {@code id}, o tipo como nome do
 * evento e o mesmo DTO da listagem em {@code data}; os emissores ficam por usuário, com timeout de 30 minutos e
 * batimento a cada 25 segundos, e são removidos ao completar, dar erro ou expirar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CanaisSse implements EventosDeNotificacao, CanaisDeEventos {

    /** Tempo de vida de um canal; o {@code EventSource} reconecta sozinho (DEC-13). */
    public static final Duration TEMPO_LIMITE = Duration.ofMinutes(30);

    private final RegistroDeEmissores registro;
    private final MapeadorDeNotificacoes mapeador;

    /** Abre um canal do usuário, com os callbacks que o removem do registro (DEC-13). */
    public SseEmitter abrir(Long usuarioId) {
        SseEmitter emissor = new SseEmitter(TEMPO_LIMITE.toMillis());
        emissor.onCompletion(() -> registro.remover(usuarioId, emissor));
        emissor.onTimeout(() -> registro.remover(usuarioId, emissor));
        emissor.onError(erro -> registro.remover(usuarioId, emissor));
        registro.adicionar(usuarioId, emissor);
        return emissor;
    }

    @Override
    public void publicar(Notificacao notificacao) {
        for (SseEmitter emissor : registro.de(notificacao.getUsuarioId())) {
            enviar(notificacao.getUsuarioId(), emissor, notificacao);
        }
    }

    /**
     * Envia uma notificação persistida a um emissor, no formato {@code id}/{@code event}/{@code data} do DEC-13.
     * Falha de escrita remove o canal, sem interromper a entrega aos outros.
     */
    public void enviar(Long usuarioId, SseEmitter emissor, Notificacao notificacao) {
        try {
            emissor.send(SseEmitter.event()
                    .id(String.valueOf(notificacao.getId()))
                    .name(notificacao.getTipo().name())
                    .data(mapeador.paraDto(notificacao), MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException excecao) {
            log.debug("Canal SSE do usuário {} encerrado durante o envio", usuarioId, excecao);
            descartar(usuarioId, emissor, excecao);
        }
    }

    /** Comentário de heartbeat em todos os canais, para manter a conexão aberta (DEC-13). */
    public void baterPonto() {
        for (Long usuarioId : registro.usuarios()) {
            for (SseEmitter emissor : registro.de(usuarioId)) {
                try {
                    emissor.send(SseEmitter.event().comment("batimento"));
                } catch (IOException | IllegalStateException excecao) {
                    descartar(usuarioId, emissor, excecao);
                }
            }
        }
    }

    @Override
    public void encerrar(Long usuarioId) {
        Set<SseEmitter> emissores = registro.de(usuarioId);
        for (SseEmitter emissor : emissores) {
            registro.remover(usuarioId, emissor);
            try {
                emissor.complete();
            } catch (IllegalStateException excecao) {
                log.debug("Canal SSE do usuário {} já estava encerrado", usuarioId, excecao);
            }
        }
    }

    private void descartar(Long usuarioId, SseEmitter emissor, Exception causa) {
        registro.remover(usuarioId, emissor);
        try {
            emissor.completeWithError(causa);
        } catch (IllegalStateException excecao) {
            log.debug("Canal SSE do usuário {} já estava encerrado", usuarioId, excecao);
        }
    }
}
