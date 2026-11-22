package br.fatec.les.livraria.vendas.aplicacao.consulta;

import br.fatec.les.livraria.compartilhado.aplicacao.Intervalo;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Período de {@code realizadoEm} informado em dias, convertido para instantes no fuso da loja (RF-VEN-009, DEC-07). O
 * último dia entra inteiro: o fim é o início do dia seguinte, exclusive.
 *
 * @param inicio início do primeiro dia, inclusive; nulo quando não há limite inferior
 * @param fimExclusivo início do dia seguinte ao último, exclusive; nulo quando não há limite superior
 */
public record PeriodoDoPedido(LocalDateTime inicio, LocalDateTime fimExclusivo) {

    /**
     * @param de primeiro dia, inclusive; pode ser nulo
     * @param ate último dia, inclusive; pode ser nulo
     * @throws ParametroInvalido se {@code de} for depois de {@code ate}, ou se {@code ate} for o último dia
     *     representável (HTTP 400, DEC-07)
     */
    public static PeriodoDoPedido dosDias(LocalDate de, LocalDate ate) {
        Intervalo.validar("realizadoEm", de, ate);
        if (LocalDate.MAX.equals(ate)) {
            throw new ParametroInvalido("realizadoEmAte", "A data final do período é inválida.");
        }
        return new PeriodoDoPedido(
                de == null ? null : de.atStartOfDay(), ate == null ? null : ate.plusDays(1).atStartOfDay());
    }
}
