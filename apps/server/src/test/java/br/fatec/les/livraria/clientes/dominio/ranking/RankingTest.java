package br.fatec.les.livraria.clientes.dominio.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

class RankingTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 24);

    @Test
    void rnCli007_notaDeValorPorFaixaComLimitesInclusivos() {
        assertThat(NotaDeValor.de(Dinheiro.ZERO)).isZero();
        assertThat(NotaDeValor.de(Dinheiro.de("0.01"))).isEqualTo(20);
        assertThat(NotaDeValor.de(Dinheiro.de("100.00"))).isEqualTo(20);
        assertThat(NotaDeValor.de(Dinheiro.de("100.01"))).isEqualTo(40);
        assertThat(NotaDeValor.de(Dinheiro.de("300.00"))).isEqualTo(40);
        assertThat(NotaDeValor.de(Dinheiro.de("600.00"))).isEqualTo(60);
        assertThat(NotaDeValor.de(Dinheiro.de("1000.00"))).isEqualTo(80);
        assertThat(NotaDeValor.de(Dinheiro.de("1000.01"))).isEqualTo(100);
    }

    @Test
    void rnCli007_notaDeFrequenciaPorFaixa() {
        assertThat(List.of(0, 1, 2, 3, 4, 5, 7, 8, 30).stream().map(NotaDeFrequencia::de))
                .containsExactly(0, 20, 40, 60, 60, 80, 80, 100, 100);
    }

    @Test
    void rnCli007_notaDeRecenciaPorFaixa() {
        assertThat(List.of(0L, 30L, 31L, 90L, 91L, 180L, 181L, 270L, 271L, 365L, 366L).stream()
                .map(NotaDeRecencia::de))
                .containsExactly(100, 100, 80, 80, 60, 60, 40, 40, 20, 20, 0);
    }

    @Test
    void rnCli007_exemploDoDec04Da68() {
        // 3 pedidos somando R$ 450,00, o último há 20 dias: 0,5·60 + 0,3·60 + 0,2·100 = 68.
        List<CompraDoCliente> compras = List.of(compra(200, "150.00"), compra(100, "150.00"), compra(20, "150.00"));

        assertThat(Ranking.calcular(compras, HOJE)).isEqualTo(68);
    }

    @Test
    void rnCli007_combinaAsNotasComOsPesos() {
        // 1 pedido de R$ 50,00 há 300 dias: 0,5·20 + 0,3·20 + 0,2·20 = 20.
        // 2 pedidos somando R$ 300,01, o último há 100 dias: 0,5·60 + 0,3·40 + 0,2·60 = 54.
        assertThat(Ranking.calcular(List.of(compra(300, "50.00")), HOJE)).isEqualTo(20);
        assertThat(Ranking.calcular(List.of(compra(100, "150.01"), compra(100, "150.00")), HOJE)).isEqualTo(54);
        // 1 pedido de R$ 10,00 há 45 dias: 0,5·20 + 0,3·20 + 0,2·80 = 32.
        assertThat(Ranking.calcular(List.of(compra(45, "10.00")), HOJE)).isEqualTo(32);
    }

    @Test
    void rnCli007_soContaAJanelaDe365DiasESemComprasDaZero() {
        assertThat(Ranking.calcular(List.of(), HOJE)).isZero();
        assertThat(Ranking.calcular(List.of(compra(366, "5000.00")), HOJE)).isZero();
        assertThat(Ranking.calcular(List.of(compra(365, "50.00")), HOJE)).isEqualTo(20);
        assertThat(JanelaDoRanking.inicio(HOJE)).isEqualTo(LocalDate.of(2025, 10, 24));
    }

    private static CompraDoCliente compra(int diasAtras, String valor) {
        return new CompraDoCliente(LocalDateTime.of(HOJE.minusDays(diasAtras), LocalTime.NOON),
                Dinheiro.de(valor));
    }
}
