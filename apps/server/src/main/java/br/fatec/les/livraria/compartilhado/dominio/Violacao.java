package br.fatec.les.livraria.compartilhado.dominio;

import java.util.Objects;

/**
 * Uma violação de regra de negócio.
 *
 * @param codigo ID do requisito violado (ex.: {@code RN-VEN-004})
 * @param mensagem texto exibível ao usuário
 */
public record Violacao(String codigo, String mensagem) {

    public Violacao {
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(mensagem, "mensagem");
    }
}
