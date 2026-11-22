package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.notificacoes.aplicacao.EntregaDepoisDoCommit;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Notifica um usuário (DEC-13, RN-VEN-014, RN-VEN-016). É o caso de uso público que os outros módulos chamam
 * diretamente, na própria transação: a notificação é persistida ali e o envio por SSE fica para depois do commit.
 */
@Component
@RequiredArgsConstructor
public class Notificar {

    private final NotificacaoRepositorio notificacoes;
    private final EntregaDepoisDoCommit entrega;
    private final Relogio relogio;

    /**
     * Persiste a notificação do comando e agenda a entrega por SSE para depois do commit.
     *
     * @return a notificação já persistida, com o id usado pelo {@code Last-Event-ID}
     */
    @Transactional
    public Notificacao executar(Comando comando) {
        Objects.requireNonNull(comando, "comando");
        Notificacao notificacao =
                Notificacao.nova(
                        comando.usuarioId(),
                        comando.tipo(),
                        comando.mensagem(),
                        comando.recurso(),
                        comando.recursoId(),
                        relogio.agora());
        Notificacao salva = notificacoes.salvar(notificacao);
        entrega.agendar(salva);
        return salva;
    }

    /**
     * Dados de uma notificação (DEC-13).
     *
     * @param usuarioId destinatário
     * @param tipo evento de origem
     * @param mensagem texto exibido ao usuário
     * @param recurso recurso referenciado (por exemplo {@code pedido}), ou nulo
     * @param recursoId id do recurso referenciado, ou nulo
     */
    public record Comando(Long usuarioId, TipoNotificacao tipo, String mensagem, String recurso, Long recursoId) {

        /** Notificação sem referência a recurso. */
        public static Comando semReferencia(Long usuarioId, TipoNotificacao tipo, String mensagem) {
            return new Comando(usuarioId, tipo, mensagem, null, null);
        }

        /** Notificação que aponta para um recurso, como o pedido ou a troca que a originou (DEC-13). */
        public static Comando sobre(
                Long usuarioId, TipoNotificacao tipo, String mensagem, String recurso, Long recursoId) {
            return new Comando(usuarioId, tipo, mensagem, recurso, recursoId);
        }
    }
}
