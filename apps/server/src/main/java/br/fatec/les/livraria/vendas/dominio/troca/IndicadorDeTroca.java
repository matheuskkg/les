package br.fatec.les.livraria.vendas.dominio.troca;

import java.util.Objects;

/**
 * Indicador calculado {@code troca} do pedido (DEC-12), ausente quando o pedido não tem troca.
 *
 * @param situacao    status da troca mais recente do pedido
 * @param abrangencia se as trocas não recusadas cobrem todo o pedido
 */
public record IndicadorDeTroca(StatusTroca situacao, AbrangenciaDaTroca abrangencia) {

    public IndicadorDeTroca {
        Objects.requireNonNull(situacao, "situacao");
        Objects.requireNonNull(abrangencia, "abrangencia");
    }
}
