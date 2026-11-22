package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.CanaisDeEventos;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Encerra os canais SSE de um usuário (DEC-13). É o caso de uso público que o logout do {@code acesso} chama: com a
 * sessão encerrada, o {@code EventSource} não deve continuar recebendo notificações.
 */
@Component
@RequiredArgsConstructor
public class EncerrarCanais {

    private final CanaisDeEventos canais;

    /** Fecha os emissores abertos do usuário; sem canal aberto, não faz nada. */
    public void executar(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        canais.encerrar(usuarioId);
    }
}
