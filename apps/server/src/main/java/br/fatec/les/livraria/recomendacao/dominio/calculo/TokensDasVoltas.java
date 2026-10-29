package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.recomendacao.dominio.modelo.VoltaDaChamada;

/**
 * Soma e descrição dos tokens das voltas de uma chamada (RNF-REC-001, DEC-03). A soma ignora as voltas sem
 * informação e fica nula quando nenhuma informou; a descrição usa {@code -} no lugar do número ausente e separa as
 * voltas por {@code ;}, como em {@code 1520/40;1810/65}.
 */
public final class TokensDasVoltas {

    private TokensDasVoltas() {
    }

    /** @return a soma e a descrição das voltas; a descrição é nula quando não houve volta */
    public static Resumo calcular(List<VoltaDaChamada> voltas) {
        return new Resumo(
                somar(voltas, VoltaDaChamada::tokensEntrada),
                somar(voltas, VoltaDaChamada::tokensSaida),
                voltas.isEmpty() ? null : voltas.stream()
                        .map(volta -> valor(volta.tokensEntrada()) + "/" + valor(volta.tokensSaida()))
                        .collect(Collectors.joining(";")));
    }

    private static @Nullable Integer somar(
            List<VoltaDaChamada> voltas, Function<VoltaDaChamada, Integer> tokens) {
        int total = 0;
        boolean informado = false;
        for (VoltaDaChamada volta : voltas) {
            Integer valor = tokens.apply(volta);
            if (valor != null && valor > 0) {
                total += valor;
                informado = true;
            }
        }
        return informado ? total : null;
    }

    private static String valor(@Nullable Integer tokens) {
        return tokens == null || tokens <= 0 ? "-" : tokens.toString();
    }

    /** Tokens somados e a descrição por volta de uma chamada. */
    public record Resumo(
            @Nullable Integer entrada,
            @Nullable Integer saida,
            @Nullable String descricao) {
    }
}
