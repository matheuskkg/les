package br.fatec.les.livraria.recomendacao.aplicacao;

import java.text.Normalizer;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * Normalização das buscas do assistente (RNF-REC-001, DEC-03): sem acentos, em minúsculas e sem espaços nas pontas,
 * para comparar sem diferenciar maiúsculas nem acentos, como a vitrine.
 */
public final class TextoNormalizado {

    private TextoNormalizado() {
    }

    /** Texto normalizado; nulo vira vazio. */
    public static String de(@Nullable String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .strip();
    }
}
