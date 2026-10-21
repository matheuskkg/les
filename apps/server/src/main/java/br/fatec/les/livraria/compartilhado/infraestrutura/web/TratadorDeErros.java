package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(ExcecaoDeNegocio.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public RespostaDeErro tratar(ExcecaoDeNegocio excecao) {
        return new RespostaDeErro("A operação viola regras de negócio.", excecao.getViolacoes());
    }
}
