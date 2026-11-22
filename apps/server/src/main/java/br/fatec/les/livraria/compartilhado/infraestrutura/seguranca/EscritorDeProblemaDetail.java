package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

/** Escreve um {@link ProblemDetail} nas respostas do Spring Security (DEC-07, DEC-08). */
@Component
@RequiredArgsConstructor
public class EscritorDeProblemaDetail {

    private final ObjectMapper json;

    public void escrever(HttpServletRequest requisicao, HttpServletResponse resposta, HttpStatus status, String detalhe)
            throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setInstance(URI.create(requisicao.getRequestURI()));

        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
