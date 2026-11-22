package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Lê o token somente do cookie {@code sessao} (DEC-08). Não há header {@code Authorization}.
 *
 * <p>Nas rotas públicas o cookie é ignorado, para que um token expirado ou de usuário inativo não transforme um GET da
 * vitrine, o autocadastro, o login ou o logout em 401. A exceção é {@code GET /api/sessao}: ele precisa do token válido
 * para dizer quem está logado; se o token for inválido, a entrada de autenticação responde 401 e apaga o cookie.
 */
@Component
@RequiredArgsConstructor
public class ResolvedorDeTokenDaSessao implements BearerTokenResolver {

    private static final RequestMatcher API = caminho(null, "/api/**");
    private static final RequestMatcher DOCUMENTACAO = caminho(null, "/v3/**");
    private static final RequestMatcher LIVROS_PUBLICOS = caminho(HttpMethod.GET, "/api/livros/**");
    private static final RequestMatcher AUTOCADASTRO = caminho(HttpMethod.POST, "/api/clientes");
    private static final RequestMatcher SESSAO = caminho(null, "/api/sessao");
    private static final RequestMatcher SESSAO_CONSULTA = caminho(HttpMethod.GET, "/api/sessao");
    private static final RequestMatcher TESTES_E2E = caminho(null, "/api/teste/**");

    private final Environment ambiente;

    @Override
    public String resolve(HttpServletRequest requisicao) {
        if (!API.matches(requisicao) && !DOCUMENTACAO.matches(requisicao)) {
            return null;
        }
        if (SESSAO_CONSULTA.matches(requisicao)) {
            return tokenDoCookie(requisicao);
        }
        if (SESSAO.matches(requisicao)
                || DOCUMENTACAO.matches(requisicao)
                || LIVROS_PUBLICOS.matches(requisicao)
                || AUTOCADASTRO.matches(requisicao)
                || (ambiente.matchesProfiles("e2e") && TESTES_E2E.matches(requisicao))) {
            return null;
        }
        return tokenDoCookie(requisicao);
    }

    /** Valor do cookie {@code sessao}, ou nulo se ausente ou vazio; também usado no logout. */
    static String tokenDoCookie(HttpServletRequest requisicao) {
        Cookie cookie = WebUtils.getCookie(requisicao, EmissorDeToken.COOKIE);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return null;
        }
        return cookie.getValue();
    }

    private static RequestMatcher caminho(HttpMethod metodo, String padrao) {
        return PathPatternRequestMatcher.pathPattern(metodo, padrao);
    }
}
