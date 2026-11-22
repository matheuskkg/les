package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.EscritorDeProblemaDetail;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 401 em {@code ProblemDetail} quando a requisição não tem uma sessão válida (DEC-08) e apaga o cookie {@code
 * sessao}, para que um token expirado ou de usuário inativo não fique preso no navegador.
 */
@Component
@RequiredArgsConstructor
public class EntradaDeAutenticacao implements AuthenticationEntryPoint {

    private final EscritorDeProblemaDetail escritor;
    private final FabricaDeCookieDeSessao cookies;

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException excecao)
            throws IOException, ServletException {
        resposta.addHeader(HttpHeaders.SET_COOKIE, cookies.expirado().toString());
        escritor.escrever(requisicao, resposta, HttpStatus.UNAUTHORIZED, "Autenticação necessária.");
    }
}
