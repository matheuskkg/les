package br.fatec.les.livraria.vendas.dominio.troca;

import org.jspecify.annotations.Nullable;

/**
 * Motivo da recusa de uma troca pelo administrador, como chegou, alvo das regras do motivo (DEC-12).
 *
 * @param motivo motivo informado
 */
public record RecusaDeTroca(@Nullable String motivo) {

    /** Tamanho máximo do motivo, que vai inteiro na notificação ao cliente (DEC-13). */
    public static final int TAMANHO_MAXIMO_DO_MOTIVO = 300;
}
