package br.fatec.les.livraria.clientes.dominio.ranking;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

/**
 * Ranking do cliente de 0 a 100 (RN-CLI-007, DEC-04): {@code 0,5·V + 0,3·F + 0,2·R} sobre as compras dos últimos
 * {@value JanelaDoRanking#DIAS} dias, arredondado para inteiro com {@code HALF_UP} (DEC-15.1). Cliente sem compras na
 * janela tem ranking 0. É calculado na consulta, nunca gravado.
 */
public final class Ranking {

    private static final BigDecimal PESO_DO_VALOR = new BigDecimal("0.5");
    private static final BigDecimal PESO_DA_FREQUENCIA = new BigDecimal("0.3");
    private static final BigDecimal PESO_DA_RECENCIA = new BigDecimal("0.2");

    private Ranking() {}

    /**
     * @param compras compras do cliente; as de fora da janela são ignoradas
     * @param hoje data do cálculo, no fuso da loja
     * @return o ranking, de 0 a 100
     */
    public static int calcular(Collection<CompraDoCliente> compras, LocalDate hoje) {
        LocalDate inicio = JanelaDoRanking.inicio(hoje);
        List<CompraDoCliente> naJanela =
                compras.stream()
                        .filter(compra ->
                                !compra.realizadoEm().toLocalDate().isBefore(inicio)
                                        && !compra.realizadoEm().toLocalDate().isAfter(hoje))
                        .toList();
        if (naJanela.isEmpty()) {
            return 0;
        }
        Dinheiro soma = naJanela.stream().map(CompraDoCliente::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
        LocalDate ultima =
                naJanela.stream()
                        .map(compra -> compra.realizadoEm().toLocalDate())
                        .max(LocalDate::compareTo)
                        .orElseThrow();
        BigDecimal ranking =
                PESO_DO_VALOR
                        .multiply(BigDecimal.valueOf(NotaDeValor.de(soma)))
                        .add(PESO_DA_FREQUENCIA.multiply(BigDecimal.valueOf(NotaDeFrequencia.de(naJanela.size()))))
                        .add(
                                PESO_DA_RECENCIA.multiply(
                                        BigDecimal.valueOf(NotaDeRecencia.de(ChronoUnit.DAYS.between(ultima, hoje)))));
        return ranking.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
