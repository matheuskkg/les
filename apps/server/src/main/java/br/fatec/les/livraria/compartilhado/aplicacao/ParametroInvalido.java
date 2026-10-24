package br.fatec.les.livraria.compartilhado.aplicacao;

import java.util.Objects;

import lombok.Getter;

/** Um parâmetro da requisição está inválido. A API responde HTTP 400 (DEC-07). */
@Getter
public class ParametroInvalido extends RuntimeException {

    private final String parametro;

    public ParametroInvalido(String parametro, String mensagem) {
        super(mensagem);
        this.parametro = Objects.requireNonNull(parametro, "parametro");
    }
}
