package br.fatec.les.livraria.recomendacao.dominio.calculo;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Detecta se o 429 de um provedor é cota diária esgotada (RNF-REC-001, DEC-03, REC-09), que só volta no dia seguinte:
 * mensagens como "per day", "free-models-per-day", "RPD" ou "daily", ou o id de cota do Gemini ({@code
 * GenerateRequestsPerDayPerProjectPerModel-FreeTier}). As marcas são palavras inteiras, para "today" ou "immediately"
 * não contarem como dia.
 */
public final class DetectarCotaDiaria {

    private static final Pattern MARCAS = Pattern.compile("\\bper[ -]?day\\b|perday|\\bdaily\\b|\\brpd\\b");

    private DetectarCotaDiaria() {}

    public static boolean ehDiaria(@Nullable String mensagem, @Nullable Duration esperaSugerida) {
        if (mensagem == null || mensagem.isBlank()) {
            return false;
        }
        String texto = mensagem.toLowerCase(Locale.ROOT);
        return MARCAS.matcher(texto).find();
    }
}
