package br.fatec.les.livraria.notificacoes.aplicacao;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Agenda a entrega de uma notificação para depois do commit da transação de quem notificou (DEC-13). O gancho fica
 * dentro de {@code notificacoes}, e não em um evento de domínio entre módulos: a chamada do {@code vendas} continua
 * sendo direta, na transação dele, e o stream só recebe a notificação que já está persistida.
 *
 * <p>Se a transação for desfeita, o {@link TransactionSynchronization#afterCommit()} não roda e nada é enviado. Fora
 * de uma transação, a escrita não tem commit a esperar e a entrega é imediata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntregaDepoisDoCommit {

    private final EventosDeNotificacao eventos;

    /** Entrega a notificação aos canais SSE do usuário assim que a transação atual fizer commit. */
    public void agendar(Notificacao notificacao) {
        Objects.requireNonNull(notificacao, "notificacao");
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publicarSemPropagar(notificacao);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publicarSemPropagar(notificacao);
            }
        });
    }

    /**
     * A escrita já fez commit: uma falha na entrega pelo stream não pode virar erro de quem notificou. A notificação
     * continua persistida e aparece na listagem e na próxima reconexão.
     */
    private void publicarSemPropagar(Notificacao notificacao) {
        try {
            eventos.publicar(notificacao);
        } catch (RuntimeException excecao) {
            log.warn("Falha ao entregar a notificação {} pelo stream", notificacao.getId(), excecao);
        }
    }
}
