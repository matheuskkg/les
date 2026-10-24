package br.fatec.les.livraria.notificacoes.aplicacao.porta;

/**
 * Porta de saída para encerrar os canais SSE de um usuário (DEC-13). O logout do módulo {@code acesso} chama o caso
 * de uso público {@code EncerrarCanais}, que usa esta porta.
 */
public interface CanaisDeEventos {

    /** Fecha os emissores abertos do usuário; sem canal aberto, não faz nada. */
    void encerrar(Long usuarioId);
}
