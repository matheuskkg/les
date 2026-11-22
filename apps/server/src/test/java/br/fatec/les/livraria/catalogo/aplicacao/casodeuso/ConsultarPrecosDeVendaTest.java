package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.catalogo.aplicacao.porta.MargemPorLivro;
import br.fatec.les.livraria.catalogo.dominio.calculo.CalcularPrecoVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarMaiorCusto;
import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Preço de venda em lote com portas em memória (RF-EST-002, RN-LIV-003). */
class ConsultarPrecosDeVendaTest {

    private final MargemPorLivroFake margens =
            new MargemPorLivroFake(
                    Map.of(
                            1L, new BigDecimal("0.2000"),
                            2L, new BigDecimal("0.3500"),
                            20L, new BigDecimal("0.3500")));

    private final MaiorCustoPorLivroFake custos =
            new MaiorCustoPorLivroFake(
                    Map.of(
                            1L, new BigDecimal("39.90"),
                            2L, new BigDecimal("47.50")));

    private final ConsultarPrecosDeVenda consultarPrecos =
            new ConsultarPrecosDeVenda(margens, new ConsultarMaiorCusto(custos), new CalcularPrecoVenda());

    @Test
    void rfEst002_calculaOPrecoComAMargemDoGrupoDeCadaLivro() {
        Map<Long, Dinheiro> precos = consultarPrecos.executar(List.of(1L, 2L));

        assertThat(precos)
                .containsEntry(1L, Dinheiro.de("47.88")) // 39.90 × 1.20
                .containsEntry(2L, Dinheiro.de("64.13")); // 47.50 × 1.35 = 64.125 → 64.13
    }

    @Test
    void livroSemEntradaNaoTemPreco() {
        Map<Long, Dinheiro> precos = consultarPrecos.executar(List.of(20L));

        assertThat(precos).doesNotContainKey(20L);
    }

    @Test
    void consultaVaziaNaoAcionaAsPortas() {
        assertThat(consultarPrecos.executar(List.of())).isEmpty();

        assertThat(margens.chamadas).isZero();
        assertThat(custos.chamadas).isZero();
    }

    @Test
    void consultaComElementoNuloEhRejeitada() {
        assertThatThrownBy(() -> consultarPrecos.executar(Arrays.asList(1L, null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("null");
    }

    @Test
    void portasRecebemOsIdsSolicitados() {
        consultarPrecos.executar(List.of(1L, 2L, 20L));

        assertThat(margens.idsRecebidos).containsExactly(1L, 2L, 20L);
        assertThat(custos.idsRecebidos).containsExactly(1L, 2L, 20L);
    }

    private static final class MargemPorLivroFake implements MargemPorLivro {

        private final Map<Long, BigDecimal> valores;
        private final List<Long> idsRecebidos = new ArrayList<>();
        private int chamadas;

        private MargemPorLivroFake(Map<Long, BigDecimal> valores) {
            this.valores = valores;
        }

        @Override
        public Map<Long, BigDecimal> buscar(Collection<Long> livroIds) {
            chamadas++;
            idsRecebidos.addAll(livroIds);
            return valores.entrySet().stream()
                    .filter(entrada -> livroIds.contains(entrada.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    private static final class MaiorCustoPorLivroFake implements MaiorCustoPorLivro {

        private final Map<Long, BigDecimal> valores;
        private final List<Long> idsRecebidos = new ArrayList<>();
        private int chamadas;

        private MaiorCustoPorLivroFake(Map<Long, BigDecimal> valores) {
            this.valores = valores;
        }

        @Override
        public Map<Long, BigDecimal> buscar(Collection<Long> livroIds) {
            chamadas++;
            idsRecebidos.addAll(livroIds);
            return valores.entrySet().stream()
                    .filter(entrada -> livroIds.contains(entrada.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
