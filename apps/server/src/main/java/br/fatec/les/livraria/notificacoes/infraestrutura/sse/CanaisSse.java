package br.fatec.les.livraria.notificacoes.infraestrutura.sse;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.CanaisDeEventos;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.infraestrutura.web.MapeadorDeNotificacoes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Canais SSE das notificações (DEC-13). Cada evento se chama {@code notificacao}, leva o id da notificação em {@code
 * id} e o mesmo DTO da listagem em {@code data}; os emissores ficam por usuário, com timeout de 30 minutos e batimento
 * a cada 25 segundos, e são removidos ao completar, dar erro ou expirar. Cada emissor guarda os ids já enviados, para
 * que a reconexão com {@code Last-Event-ID} não repita o que chegou ao vivo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CanaisSse implements EventosDeNotificacao, CanaisDeEventos {

    /** Tempo de vida de um canal; o {@code EventSource} reconecta sozinho (DEC-13). */
    public static final Duration TEMPO_LIMITE = Duration.ofMinutes(30);

    /** Nome único dos eventos do stream; o {@code web} escuta só ele (DEC-13). */
    public static final String EVENTO = "notificacao";

    private final RegistroDeEmissores registro;
    private final MapeadorDeNotificacoes mapeador;
    private final Map<SseEmitter, Set<Long>> enviados = new ConcurrentHashMap<>();

    /** Abre um canal do usuário, com os callbacks que o removem do registro (DEC-13). */
    public SseEmitter abrir(Long usuarioId) {
        SseEmitter emissor = new SseEmitter(TEMPO_LIMITE.toMillis());
        enviados.put(emissor, ConcurrentHashMap.newKeySet());
        emissor.onCompletion(() -> esquecer(usuarioId, emissor));
        emissor.onTimeout(() -> fechar(usuarioId, emissor));
        emissor.onError(erro -> esquecer(usuarioId, emissor));
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
     * Envia uma notificação persistida a um emissor, no formato {@code id}/{@code event}/{@code data} do DEC-13. Falha
     * de escrita remove o canal, sem interromper a entrega aos outros; uma notificação já enviada a este emissor não é
     * repetida.
     */
    public void enviar(Long usuarioId, SseEmitter emissor, Notificacao notificacao) {
        Set<Long> jaEnviados = enviados.get(emissor);
        if (jaEnviados == null || !jaEnviados.add(notificacao.getId())) {
            return;
        }
        try {
            emissor.send(
                    SseEmitter.event()
                            .id(String.valueOf(notificacao.getId()))
                            .name(EVENTO)
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
        for (SseEmitter emissor : registro.de(usuarioId)) {
            fechar(usuarioId, emissor);
        }
    }

    /** Fecha só este emissor, sem afetar os outros canais do usuário. */
    public void fechar(Long usuarioId, SseEmitter emissor) {
        esquecer(usuarioId, emissor);
        try {
            emissor.complete();
        } catch (IllegalStateException excecao) {
            log.debug("Canal SSE do usuário {} já estava encerrado", usuarioId, excecao);
        }
    }

    private void esquecer(Long usuarioId, SseEmitter emissor) {
        registro.remover(usuarioId, emissor);
        enviados.remove(emissor);
    }

    private void descartar(Long usuarioId, SseEmitter emissor, Exception causa) {
        esquecer(usuarioId, emissor);
        try {
            emissor.completeWithError(causa);
        } catch (IllegalStateException excecao) {
            log.debug("Canal SSE do usuário {} já estava encerrado", usuarioId, excecao);
        }
    }
}
