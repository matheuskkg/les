package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;

/** Implementação do autor das escritas (RNF-GER-002) sobre o contexto do Spring Security e da requisição. */
@Component
public class UsuarioAtualDoContexto implements UsuarioAtual {

    @Override
    public Optional<Long> usuarioId() {
        if (foraDeRequisicaoHttp()) {
            return Optional.empty();
        }
        return autenticado().map(UsuarioAutenticado::usuarioId);
    }

    @Override
    public String usuario() {
        if (foraDeRequisicaoHttp()) {
            return SISTEMA;
        }
        return autenticado().map(UsuarioAutenticado::email).orElse(ANONIMO);
    }

    private static boolean foraDeRequisicaoHttp() {
        return RequestContextHolder.getRequestAttributes() == null;
    }

    private static Optional<UsuarioAutenticado> autenticado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            return Optional.empty();
        }
        return autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario
                ? Optional.of(usuario)
                : Optional.empty();
    }
}
