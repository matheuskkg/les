package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Responde 403 em {@code ProblemDetail} quando falta o papel exigido ou quando o token CSRF é inválido (DEC-08).
 */
@Component
@RequiredArgsConstructor
public class RespostaDeAcessoNegado implements AccessDeniedHandler {

    private final EscritorDeProblemaDetail escritor;

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
            AccessDeniedException excecao) throws IOException, ServletException {
        String detalhe = excecao instanceof CsrfException
                ? "Token CSRF ausente ou inválido."
                : "Acesso negado.";
        escritor.escrever(requisicao, resposta, HttpStatus.FORBIDDEN, detalhe);
    }
}
