package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Força a leitura do token CSRF adiado logo depois do {@link CsrfFilter}. Sem isso, um GET que não use o token (como
 * a carga inicial da SPA) não grava o cookie {@code XSRF-TOKEN} (DEC-08).
 */
public class FiltroDeCookieCsrf extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        Object atributo = requisicao.getAttribute(DeferredCsrfToken.class.getName());
        if (atributo instanceof DeferredCsrfToken tokenAdiado) {
            tokenAdiado.get();
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
