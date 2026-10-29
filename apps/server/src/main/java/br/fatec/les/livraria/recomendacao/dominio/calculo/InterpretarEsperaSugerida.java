package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Interpreta o tempo sugerido por um provedor que respondeu 429 (RNF-REC-001, DEC-03, REC-09): o cabeçalho
 * {@code Retry-After}, em segundos ou data HTTP, e o texto da mensagem, como "try again in 7.5s",
 * "Please try again in 1m30s" ou "retry in 13s".
 */
public final class InterpretarEsperaSugerida {

    private static final Pattern SEGUNDOS = Pattern.compile("\\d+(?:[.,]\\d+)?");
    private static final Pattern TRECHO_DA_MENSAGEM = Pattern.compile(
            "(?i)(?:try again|retry)\\s+in\\s+([0-9.,]+\\s*(?:ms|h|m|s)(?:\\s*[0-9.,]+\\s*(?:ms|h|m|s))*)");
    private static final Pattern PARTE = Pattern.compile("(?i)([0-9]+(?:[.,][0-9]+)?)\\s*(ms|h|m|s)");

    private InterpretarEsperaSugerida() {
    }

    /** Tempo do cabeçalho {@code Retry-After}: segundos ou data HTTP, relativa a {@code agora}. */
    public static Optional<Duration> deCabecalho(@Nullable String valor, Instant agora) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        String texto = valor.trim();
        if (SEGUNDOS.matcher(texto).matches()) {
            return Optional.of(emSegundos(texto));
        }
        try {
            ZonedDateTime data = ZonedDateTime.parse(texto, DateTimeFormatter.RFC_1123_DATE_TIME);
            return Optional.of(Duration.between(agora, data.toInstant()));
        } catch (DateTimeParseException invalida) {
            return Optional.empty();
        }
    }

    /** Tempo citado no texto da mensagem, com {@code ms}, {@code s}, {@code m} e {@code h} combinados. */
    public static Optional<Duration> daMensagem(@Nullable String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return Optional.empty();
        }
        Matcher trecho = TRECHO_DA_MENSAGEM.matcher(mensagem);
        if (!trecho.find()) {
            return Optional.empty();
        }
        Matcher parte = PARTE.matcher(trecho.group(1));
        long milissegundos = 0;
        boolean encontrou = false;
        while (parte.find()) {
            encontrou = true;
            milissegundos += emMilissegundos(parte.group(1), parte.group(2));
        }
        return encontrou ? Optional.of(Duration.ofMillis(milissegundos)) : Optional.empty();
    }

    private static Duration emSegundos(String valor) {
        return Duration.ofMillis(new BigDecimal(valor.replace(',', '.')).multiply(BigDecimal.valueOf(1000))
                .longValue());
    }

    private static long emMilissegundos(String valor, String unidade) {
        BigDecimal quantidade = new BigDecimal(valor.replace(',', '.'));
        BigDecimal fator = switch (unidade.toLowerCase(Locale.ROOT)) {
            case "ms" -> BigDecimal.ONE;
            case "s" -> BigDecimal.valueOf(1000);
            case "m" -> BigDecimal.valueOf(60_000);
            case "h" -> BigDecimal.valueOf(3_600_000);
            default -> BigDecimal.ZERO;
        };
        return quantidade.multiply(fator).longValue();
    }
}
