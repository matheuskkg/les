package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import java.util.Optional;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Descobre o usuário do cookie {@code sessao} no logout, sem tornar a rota autenticada (DEC-08, DEC-13).
 *
 * <p>{@code DELETE /api/sessao} é público para que um token expirado ou de usuário inativo continue respondendo 204 e
 * apagando o cookie; por isso o filtro do token não o lê. Aqui o cookie é validado só para achar o dono dos canais SSE
 * a fechar: token ausente ou inválido é simplesmente ignorado.
 */
@Component
@RequiredArgsConstructor
public class IdentidadeDoLogout {

    private final JwtDecoder jwtDecoder;
    private final Converter<Jwt, AbstractAuthenticationToken> conversorDeJwt;

    /** Id do usuário do token válido do cookie, ou vazio se não há cookie ou ele não vale mais. */
    public Optional<Long> usuarioId(HttpServletRequest requisicao) {
        Cookie cookie = WebUtils.getCookie(requisicao, EmissorDeToken.COOKIE);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return Optional.empty();
        }
        try {
            AbstractAuthenticationToken autenticacao = conversorDeJwt.convert(jwtDecoder.decode(cookie.getValue()));
            return autenticacao.getPrincipal() instanceof PrincipalDaSessao principal
                    ? Optional.of(principal.usuarioId())
                    : Optional.empty();
        } catch (JwtException | AuthenticationException excecao) {
            return Optional.empty();
        }
    }
}
