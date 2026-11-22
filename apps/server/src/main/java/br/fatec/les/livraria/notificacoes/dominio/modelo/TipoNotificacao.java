package br.fatec.les.livraria.notificacoes.dominio.modelo;

/**
 * Tipos de notificação do {@code vendas} (DEC-13, RN-VEN-014, RN-VEN-016). O tipo identifica o evento para o {@code
 * web}, que recarrega a tela afetada; ele também é o nome do evento no stream SSE.
 *
 * <p>Um tipo pertence a um destinatário: os de cliente são criados pelo {@code Notificar} e os de admin pelo {@code
 * NotificarAdministradores}, que os replica para cada usuário {@code ADMIN}.
 */
public enum TipoNotificacao {

    /** Aviso de expiração do carrinho, antes do prazo (RN-VEN-014). */
    CARRINHO_EXPIRANDO(Destinatario.CLIENTE),
    /** Aviso de expiração do checkout, antes do prazo (RN-VEN-014). */
    CHECKOUT_EXPIRANDO(Destinatario.CLIENTE),
    /** Itens removidos do carrinho por expiração (RN-VEN-014). */
    CARRINHO_EXPIRADO(Destinatario.CLIENTE),
    /** Itens removidos e reservas liberadas por expiração do checkout (RN-VEN-014, RN-VEN-002). */
    CHECKOUT_EXPIRADO(Destinatario.CLIENTE),
    /** Resultado do pagamento: pedido aprovado. */
    PEDIDO_APROVADO(Destinatario.CLIENTE),
    /** Resultado do pagamento: pedido reprovado. */
    PEDIDO_REPROVADO(Destinatario.CLIENTE),
    /** Pedido despachado. */
    PEDIDO_DESPACHADO(Destinatario.CLIENTE),
    /** Pedido entregue. */
    PEDIDO_ENTREGUE(Destinatario.CLIENTE),
    /** Troca autorizada pelo administrador (RN-VEN-016). */
    TROCA_AUTORIZADA(Destinatario.CLIENTE),
    /** Troca recusada pelo administrador, com o motivo na mensagem. */
    TROCA_RECUSADA(Destinatario.CLIENTE),
    /** Troca concluída, com o cupom de troca gerado. */
    TROCA_CONCLUIDA(Destinatario.CLIENTE),
    /** Nova troca para o administrador autorizar (RN-VEN-016). */
    TROCA_SOLICITADA(Destinatario.ADMIN),
    /** Pedido aprovado, aguardando despacho pelo administrador. */
    PEDIDO_AGUARDANDO_DESPACHO(Destinatario.ADMIN);

    /** Quem recebe o tipo de notificação (DEC-13). */
    public enum Destinatario {
        CLIENTE,
        ADMIN
    }

    private final Destinatario destinatario;

    TipoNotificacao(Destinatario destinatario) {
        this.destinatario = destinatario;
    }

    public Destinatario destinatario() {
        return destinatario;
    }
}
