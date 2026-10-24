package br.fatec.les.livraria.notificacoes.infraestrutura.web;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.ListarNotificacoesDesde;
import br.fatec.les.livraria.notificacoes.infraestrutura.sse.CanaisSse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Stream SSE das notificações do cliente e do administrador (DEC-13). O evento leva o id da notificação em
 * {@code id}, o tipo em {@code event} e o DTO da listagem em {@code data}. Na reconexão, o {@code Last-Event-ID}
 * faz o servidor reenviar as notificações persistidas do usuário com id maior.
 */
@RestController
@RequiredArgsConstructor
public class EventosController {

    private final CanaisSse canais;
    private final ListarNotificacoesDesde listarNotificacoesDesde;

    @GetMapping(path = "/api/me/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Abre o stream SSE das notificações do cliente")
    public SseEmitter meusEventos(Authentication autenticacao,
            @RequestHeader(name = "Last-Event-ID", required = false) Long ultimoEvento) {
        return abrir(UsuarioDaRequisicao.id(autenticacao), ultimoEvento);
    }

    @GetMapping(path = "/api/admin/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Abre o stream SSE das notificações do administrador")
    public SseEmitter eventosDoAdmin(Authentication autenticacao,
            @RequestHeader(name = "Last-Event-ID", required = false) Long ultimoEvento) {
        return abrir(UsuarioDaRequisicao.id(autenticacao), ultimoEvento);
    }

    /**
     * Registra o canal antes de reenviar o intervalo do {@code Last-Event-ID}, para que uma notificação publicada
     * durante a conexão não se perca. A partir daí, só chegam os eventos entregues depois do commit.
     */
    private SseEmitter abrir(Long usuarioId, Long ultimoEvento) {
        SseEmitter emissor = canais.abrir(usuarioId);
        if (ultimoEvento == null) {
            return emissor;
        }
        try {
            listarNotificacoesDesde.executar(usuarioId, ultimoEvento)
                    .forEach(notificacao -> canais.enviar(usuarioId, emissor, notificacao));
            return emissor;
        } catch (RuntimeException excecao) {
            canais.encerrar(usuarioId);
            throw excecao;
        }
    }
}
