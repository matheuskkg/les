package br.fatec.les.livraria.vendas.dominio.troca;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Recusa de uma troca pelo administrador, alvo das regras do motivo (DEC-12).
 *
 * @param troca  a troca a recusar
 * @param motivo motivo informado, como chegou
 */
public record RecusaDeTroca(Troca troca, @Nullable String motivo) {

    /** Tamanho máximo do motivo, que vai inteiro na notificação ao cliente (DEC-13). */
    public static final int TAMANHO_MAXIMO_DO_MOTIVO = 300;

    public RecusaDeTroca {
        Objects.requireNonNull(troca, "troca");
    }
}
