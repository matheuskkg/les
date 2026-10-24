package br.fatec.les.livraria.notificacoes.aplicacao;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.EventosDeNotificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import lombok.RequiredArgsConstructor;

/**
 * Agenda a entrega de uma notificação para depois do commit da transação de quem notificou (DEC-13). O gancho fica
 * dentro de {@code notificacoes}, e não em um evento de domínio entre módulos: a chamada do {@code vendas} continua
 * sendo direta, na transação dele, e o stream só recebe a notificação que já está persistida.
 *
 * <p>Se a transação for desfeita, o {@link TransactionSynchronization#afterCommit()} não roda e nada é enviado. Fora
 * de uma transação, a escrita não tem commit a esperar e a entrega é imediata.
 */
@Component
@RequiredArgsConstructor
public class EntregaDepoisDoCommit {

    private final EventosDeNotificacao eventos;

    /** Entrega a notificação aos canais SSE do usuário assim que a transação atual fizer commit. */
    public void agendar(Notificacao notificacao) {
        Objects.requireNonNull(notificacao, "notificacao");
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventos.publicar(notificacao);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventos.publicar(notificacao);
            }
        });
    }
}
