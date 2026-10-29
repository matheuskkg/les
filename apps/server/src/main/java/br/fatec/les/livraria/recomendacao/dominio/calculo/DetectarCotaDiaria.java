package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Detecta se o 429 de um provedor é cota diária esgotada (RNF-REC-001, DEC-03, REC-09), que só volta no dia
 * seguinte: mensagens como "per day", "free-models-per-day", "RPD" ou "daily", ou um tempo sugerido maior que 5 min
 * acompanhado de indicação de dia.
 */
public final class DetectarCotaDiaria {

    private static final Duration MAXIMA = Duration.ofMinutes(5);
    private static final List<String> MARCAS = List.of(
            "per day", "per-day", "free-models-per-day", "daily", "requests per day", "tokens per day");
    private static final Pattern RPD = Pattern.compile("\\brpd\\b");

    private DetectarCotaDiaria() {
    }

    public static boolean ehDiaria(@Nullable String mensagem, @Nullable Duration esperaSugerida) {
        if (mensagem == null || mensagem.isBlank()) {
            return false;
        }
        String texto = mensagem.toLowerCase(Locale.ROOT);
        if (MARCAS.stream().anyMatch(texto::contains) || RPD.matcher(texto).find()) {
            return true;
        }
        return esperaSugerida != null && esperaSugerida.compareTo(MAXIMA) > 0
                && (texto.contains("day") || texto.contains("dia"));
    }
}
