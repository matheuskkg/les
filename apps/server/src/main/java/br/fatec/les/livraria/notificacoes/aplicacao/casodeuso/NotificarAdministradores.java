package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ListarAdministradores;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import lombok.RequiredArgsConstructor;

/**
 * Notifica todos os usuários {@code ADMIN} (DEC-13, RN-VEN-016). Pede a lista ao {@code acesso} pelo caso de uso
 * público {@code ListarAdministradores} e cria uma notificação por administrador, na transação de quem chamou; cada
 * uma é entregue por SSE só depois do commit.
 */
@Component
@RequiredArgsConstructor
public class NotificarAdministradores {

    private final ListarAdministradores listarAdministradores;
    private final Notificar notificar;

    /** Persiste a notificação para cada administrador e devolve as notificações criadas. */
    @Transactional
    public List<Notificacao> executar(Comando comando) {
        Objects.requireNonNull(comando, "comando");
        return listarAdministradores.executar().stream()
                .map(usuarioId -> notificar.executar(new Notificar.Comando(usuarioId, comando.tipo(),
                        comando.mensagem(), comando.recurso(), comando.recursoId())))
                .toList();
    }

    /**
     * Dados da notificação dos administradores (DEC-13).
     *
     * @param tipo      evento de origem
     * @param mensagem  texto exibido ao administrador
     * @param recurso   recurso referenciado (por exemplo {@code troca}), ou nulo
     * @param recursoId id do recurso referenciado, ou nulo
     */
    public record Comando(TipoNotificacao tipo, String mensagem, String recurso, Long recursoId) {

        public Comando {
            Objects.requireNonNull(tipo, "tipo");
            if (tipo.destinatario() != TipoNotificacao.Destinatario.ADMIN) {
                throw new IllegalArgumentException("O tipo " + tipo + " não é destinado aos administradores.");
            }
        }

        /** Notificação sem referência a recurso. */
        public static Comando semReferencia(TipoNotificacao tipo, String mensagem) {
            return new Comando(tipo, mensagem, null, null);
        }

        /** Notificação que aponta para um recurso, como a troca que precisa de autorização (RN-VEN-016). */
        public static Comando sobre(TipoNotificacao tipo, String mensagem, String recurso, Long recursoId) {
            return new Comando(tipo, mensagem, recurso, recursoId);
        }
    }
}
