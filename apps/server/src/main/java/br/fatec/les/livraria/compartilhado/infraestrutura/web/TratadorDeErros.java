package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import lombok.extern.slf4j.Slf4j;

/**
 * Responde os erros da API com {@link ProblemDetail} (RFC 9457). As exceções do Spring MVC (400, 404, 405, 415...)
 * são tratadas pela classe-mãe.
 */
@Slf4j
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ExcecaoDeNegocio.class)
    public ProblemDetail tratar(ExcecaoDeNegocio excecao) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT, "A operação viola regras de negócio.");
        problema.setProperty("violacoes", excecao.getViolacoes());
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratar(Exception excecao) {
        log.error("Erro inesperado", excecao);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado no servidor.");
    }
}
