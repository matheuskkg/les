package br.fatec.les.livraria.notificacoes.infraestrutura.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro em memória dos emissores SSE por usuário (DEC-13). Uma instância só, como a topologia de execução (DEC-09).
 * O mapa e os conjuntos são concorrentes porque a abertura acontece na thread da requisição e o envio, o batimento e o
 * encerramento rodam em outras threads.
 */
@Component
public class RegistroDeEmissores {

    private final Map<Long, Set<SseEmitter>> emissores = new ConcurrentHashMap<>();

    /** Registra um emissor recém-aberto do usuário. */
    public void adicionar(Long usuarioId, SseEmitter emissor) {
        // A inclusão fica dentro do compute para não cair num conjunto que um remover() concorrente já tirou do mapa.
        emissores.compute(
                usuarioId,
                (id, canais) -> {
                    Set<SseEmitter> atuais = canais == null ? ConcurrentHashMap.newKeySet() : canais;
                    atuais.add(emissor);
                    return atuais;
                });
    }

    /** Remove o emissor; o usuário sai do mapa quando não tem mais canal aberto. */
    public void remover(Long usuarioId, SseEmitter emissor) {
        emissores.computeIfPresent(
                usuarioId,
                (id, canais) -> {
                    canais.remove(emissor);
                    return canais.isEmpty() ? null : canais;
                });
    }

    /** Emissores abertos do usuário, em uma cópia que pode ser percorrida sem lock. */
    public Set<SseEmitter> de(Long usuarioId) {
        return Set.copyOf(emissores.getOrDefault(usuarioId, Set.of()));
    }

    /** Usuários com ao menos um canal aberto, para o batimento periódico. */
    public Set<Long> usuarios() {
        return Set.copyOf(emissores.keySet());
    }

    /** Quantidade de canais abertos do usuário, para observar o encerramento no logout (DEC-13). */
    public int contar(Long usuarioId) {
        return emissores.getOrDefault(usuarioId, Set.of()).size();
    }
}
