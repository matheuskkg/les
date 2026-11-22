package br.fatec.les.livraria.recomendacao.dominio.calculo;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calcula por quanto tempo um provedor que respondeu 429 fica fora da cadeia (RNF-REC-001, DEC-03, REC-09): o tempo
 * sugerido, limitado entre 10 s e 5 min, 5 min quando não há indicação e, na cota diária, até a meia-noite do fuso do
 * relógio da aplicação, com pelo menos 10 s.
 */
public final class CalcularEsperaDeCota {

    static final Duration MINIMA = Duration.ofSeconds(10);
    static final Duration MAXIMA = Duration.ofMinutes(5);

    private CalcularEsperaDeCota() {}

    /**
     * @param esperaSugerida tempo informado pelo provedor, ou {@code null} se ele não informou
     * @param cotaDiaria indica que a cota é diária e só volta no dia seguinte
     * @param agora data/hora local do {@code Relogio}
     * @return a espera efetiva, nunca negativa
     */
    public static Duration calcular(@Nullable Duration esperaSugerida, boolean cotaDiaria, LocalDateTime agora) {
        if (cotaDiaria) {
            Duration ateOProximoDia = Duration.between(agora, agora.toLocalDate().plusDays(1).atStartOfDay());
            return ateOProximoDia.compareTo(MINIMA) < 0 ? MINIMA : ateOProximoDia;
        }
        if (esperaSugerida == null) {
            return MAXIMA;
        }
        if (esperaSugerida.compareTo(MINIMA) < 0) {
            return MINIMA;
        }
        if (esperaSugerida.compareTo(MAXIMA) > 0) {
            return MAXIMA;
        }
        return esperaSugerida;
    }
}
