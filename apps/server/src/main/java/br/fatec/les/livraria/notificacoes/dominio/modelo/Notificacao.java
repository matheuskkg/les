package br.fatec.les.livraria.notificacoes.dominio.modelo;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Notificação persistida de um usuário (DEC-13, DEC-16, RN-VEN-014, RN-VEN-016). O módulo é genérico: não conhece
 * pedido, troca nem carrinho; a referência opcional ({@code recurso} e {@code recursoId}) é só um par de valores que o
 * {@code web} usa para montar o link.
 *
 * <p>O {@code usuarioId} é a referência ao usuário do módulo {@code acesso} apenas pelo id (DEC-16). A notificação
 * nasce não lida e só pode ser marcada como lida uma vez; a segunda marcação preserva o instante original.
 */
@Getter
public class Notificacao {

    /** Limite da coluna {@code mensagem} no MER (DEC-14, DEC-16). */
    public static final int TAMANHO_MAXIMO_DA_MENSAGEM = 500;

    /** Limite da coluna {@code recurso} no MER (DEC-14, DEC-16). */
    public static final int TAMANHO_MAXIMO_DO_RECURSO = 40;

    private final Long id;
    private final Long usuarioId;
    private final TipoNotificacao tipo;
    private final String mensagem;
    private final String recurso;
    private final Long recursoId;
    private final LocalDateTime criadaEm;
    private LocalDateTime lidaEm;

    /**
     * @param id nulo enquanto a notificação não foi persistida
     * @param usuarioId destinatário; obrigatório
     * @param tipo evento que originou a notificação; obrigatório
     * @param mensagem texto exibido ao usuário; obrigatório, de 1 a 500 caracteres
     * @param recurso nome do recurso referenciado (por exemplo {@code pedido}); opcional, mas exige {@code recursoId}
     * @param recursoId id do recurso referenciado; opcional, mas exige {@code recurso}
     * @param criadaEm instante de criação; obrigatório
     * @param lidaEm instante da leitura; nulo enquanto não lida
     */
    public Notificacao(
            Long id,
            Long usuarioId,
            TipoNotificacao tipo,
            String mensagem,
            String recurso,
            Long recursoId,
            LocalDateTime criadaEm,
            LocalDateTime lidaEm) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.mensagem = mensagem(mensagem);
        this.recurso = recurso(recurso, recursoId);
        this.recursoId = recursoId;
        this.criadaEm = Objects.requireNonNull(criadaEm, "criadaEm");
        this.lidaEm = lidaEm;
    }

    /** Cria uma notificação ainda não persistida e não lida (DEC-13). */
    public static Notificacao nova(
            Long usuarioId,
            TipoNotificacao tipo,
            String mensagem,
            String recurso,
            Long recursoId,
            LocalDateTime criadaEm) {
        return new Notificacao(null, usuarioId, tipo, mensagem, recurso, recursoId, criadaEm, null);
    }

    /** Indica se a notificação está no histórico não lido do usuário (RN-VEN-014, RN-VEN-016). */
    public boolean lida() {
        return lidaEm != null;
    }

    /** Indica se a notificação pertence ao usuário; a leitura só vale para as próprias notificações (DEC-07). */
    public boolean pertenceA(Long usuarioId) {
        return Objects.equals(this.usuarioId, usuarioId);
    }

    /**
     * Marca a notificação como lida em {@code agora}. É idempotente: uma notificação já lida mantém o instante da
     * primeira leitura.
     */
    public void marcarComoLida(LocalDateTime agora) {
        Objects.requireNonNull(agora, "agora");
        if (lidaEm == null) {
            lidaEm = agora;
        }
    }

    private static String mensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem da notificação é obrigatória.");
        }
        String limpa = mensagem.strip();
        if (limpa.length() > TAMANHO_MAXIMO_DA_MENSAGEM) {
            throw new IllegalArgumentException(
                    "A mensagem da notificação deve ter no máximo " + TAMANHO_MAXIMO_DA_MENSAGEM + " caracteres.");
        }
        return limpa;
    }

    private static String recurso(String recurso, Long recursoId) {
        if (recurso == null && recursoId == null) {
            return null;
        }
        if (recurso == null || recursoId == null) {
            throw new IllegalArgumentException("A referência da notificação exige o recurso e o id juntos.");
        }
        String limpo = recurso.strip();
        if (limpo.isEmpty()) {
            throw new IllegalArgumentException("O recurso da notificação é obrigatório quando há referência.");
        }
        if (limpo.length() > TAMANHO_MAXIMO_DO_RECURSO) {
            throw new IllegalArgumentException(
                    "O recurso da notificação deve ter no máximo " + TAMANHO_MAXIMO_DO_RECURSO + " caracteres.");
        }
        return limpo;
    }
}
