package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
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

    @ExceptionHandler(ParametroInvalido.class)
    public ProblemDetail tratar(ParametroInvalido excecao) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excecao.getMessage());
        problema.setProperty("parametro", excecao.getParametro());
        return problema;
    }

    @ExceptionHandler(ExcecaoDeConflito.class)
    public ProblemDetail tratar(ExcecaoDeConflito excecao) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail tratar(OptimisticLockingFailureException excecao) {
        log.warn("Conflito de alteração concorrente", excecao);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O registro foi alterado por outra operação. Tente novamente.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratar(DataIntegrityViolationException excecao) {
        if (violacaoDeUnicidade(excecao)) {
            log.warn("Conflito de unicidade", excecao);
            return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                    "O registro já existe ou conflita com outro cadastro.");
        }
        log.error("Violação de integridade não prevista", excecao);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado no servidor.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratar(Exception excecao) {
        log.error("Erro inesperado", excecao);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado no servidor.");
    }

    /**
     * Sinais tipados de unicidade: a exceção do Spring, a do Hibernate com {@link ConstraintKind#UNIQUE} ou o
     * SQLState {@code 23505} (PostgreSQL e H2) em qualquer ponto da cadeia de causas.
     */
    private static boolean violacaoDeUnicidade(Throwable erro) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (atual instanceof DuplicateKeyException) {
                return true;
            }
            if (atual instanceof ConstraintViolationException violacao
                    && violacao.getKind() == ConstraintKind.UNIQUE) {
                return true;
            }
            if (atual instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
