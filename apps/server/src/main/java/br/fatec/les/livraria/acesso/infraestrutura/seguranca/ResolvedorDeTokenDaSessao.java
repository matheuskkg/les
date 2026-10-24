package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Lê o token somente do cookie {@code sessao} (DEC-08). Não há header {@code Authorization}.
 */
@Component
public class ResolvedorDeTokenDaSessao implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest requisicao) {
        Cookie cookie = WebUtils.getCookie(requisicao, EmissorDeToken.COOKIE);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return null;
        }
        return cookie.getValue();
    }
}
