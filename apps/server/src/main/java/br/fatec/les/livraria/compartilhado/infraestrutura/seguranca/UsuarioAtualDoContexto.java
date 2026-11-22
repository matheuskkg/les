package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Autor;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

/** Implementação do autor das escritas (RNF-GER-002) sobre o contexto do Spring Security e da requisição. */
@Component
public class UsuarioAtualDoContexto implements UsuarioAtual {

    @Override
    public Autor autor() {
        if (foraDeRequisicaoHttp()) {
            return new Autor(null, SISTEMA);
        }
        return autenticado()
                .map(usuario -> new Autor(usuario.usuarioId(), usuario.email()))
                .orElseGet(() -> new Autor(null, ANONIMO));
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
