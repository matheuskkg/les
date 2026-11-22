package br.fatec.les.livraria.recomendacao.dominio.modelo;

import org.jspecify.annotations.Nullable;

/**
 * Uma chamada HTTP feita a um provedor de IA dentro de uma mensagem (RNF-REC-001, DEC-03). Guarda os tokens de entrada
 * e de saída informados naquela volta; os dois ficam nulos quando o provedor não os informou, inclusive nas falhas.
 */
public record VoltaDaChamada(@Nullable Integer tokensEntrada, @Nullable Integer tokensSaida) {

    /** Volta sem a contagem de tokens, como a de uma falha HTTP ou a de um provedor que não informa o uso. */
    public static VoltaDaChamada semTokens() {
        return new VoltaDaChamada(null, null);
    }
}
