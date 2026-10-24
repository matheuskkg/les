package br.fatec.les.livraria.notificacoes.infraestrutura.web;

import org.springframework.security.core.Authentication;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;

/**
 * Lê o usuário da sessão para as rotas de notificação (DEC-07, DEC-08). O id vem sempre do principal autenticado,
 * nunca de um parâmetro da URL; as rotas são protegidas por papel na configuração de segurança.
 */
final class UsuarioDaRequisicao {

    private UsuarioDaRequisicao() {
    }

    static Long id(Authentication autenticacao) {
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new IllegalStateException("Rota autenticada sem usuário na sessão.");
        }
        return usuario.usuarioId();
    }
}
