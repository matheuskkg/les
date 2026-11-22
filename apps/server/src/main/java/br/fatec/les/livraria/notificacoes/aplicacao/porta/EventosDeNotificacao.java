package br.fatec.les.livraria.notificacoes.aplicacao.porta;

import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

/**
 * Porta de saída do stream SSE (DEC-13). A entrega é feita só depois do commit da notificação, pelo {@code
 * EntregaDepoisDoCommit}; o adapter mantém os emissores em memória por usuário.
 */
public interface EventosDeNotificacao {

    /** Entrega uma notificação persistida aos emissores abertos do usuário; sem canal aberto, não faz nada. */
    void publicar(Notificacao notificacao);
}
