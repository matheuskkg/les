package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Resposta estruturada de um provedor de IA (RNF-REC-001, DEC-03), com o conteúdo já validado pelo adapter e os
 * tokens informados pelo provedor, quando houver. O conteúdo é o modelo de domínio da operação, nunca texto livre.
 *
 * @param conteudo      resposta da operação
 * @param tokensEntrada tokens de entrada, ou {@code null} se o provedor não os informou
 * @param tokensSaida   tokens de saída, ou {@code null} se o provedor não os informou
 */
public record RespostaDoProvedor<T>(
        T conteudo,
        @Nullable Integer tokensEntrada,
        @Nullable Integer tokensSaida) {

    public RespostaDoProvedor {
        Objects.requireNonNull(conteudo, "conteudo");
    }

    /** Resposta sem a contagem de tokens, como a do provedor fake (DEC-03). */
    public static <T> RespostaDoProvedor<T> semTokens(T conteudo) {
        return new RespostaDoProvedor<>(conteudo, null, null);
    }
}
