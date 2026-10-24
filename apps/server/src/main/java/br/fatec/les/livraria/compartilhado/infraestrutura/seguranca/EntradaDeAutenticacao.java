package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** Responde 401 em {@code ProblemDetail} quando a requisição não tem uma sessão válida (DEC-08). */
@Component
@RequiredArgsConstructor
public class EntradaDeAutenticacao implements AuthenticationEntryPoint {

    private final EscritorDeProblemaDetail escritor;

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
            AuthenticationException excecao) throws IOException, ServletException {
        escritor.escrever(requisicao, resposta, HttpStatus.UNAUTHORIZED, "Autenticação necessária.");
    }
}
