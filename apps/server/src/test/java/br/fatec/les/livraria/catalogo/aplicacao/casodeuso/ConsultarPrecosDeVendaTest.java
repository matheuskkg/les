package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.catalogo.aplicacao.porta.MargemPorLivro;
import br.fatec.les.livraria.catalogo.dominio.calculo.CalcularPrecoVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarMaiorCusto;
import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;

/** Preço de venda em lote com portas em memória (RF-EST-002, RN-EST-002, RN-LIV-003). */
class ConsultarPrecosDeVendaTest {

    private final MargemPorLivroFake margens = new MargemPorLivroFake(Map.of(
            1L, new BigDecimal("0.2000"),
            2L, new BigDecimal("0.3500"),
            20L, new BigDecimal("0.3500")));

    private final MaiorCustoPorLivroFake custos = new MaiorCustoPorLivroFake(Map.of(
            1L, new BigDecimal("39.90"),
            2L, new BigDecimal("47.50")));

    private final ConsultarPrecosDeVenda consultarPrecos = new ConsultarPrecosDeVenda(
            margens, new ConsultarMaiorCusto(custos), new CalcularPrecoVenda());

    @Test
    void rfEst002_calculaOMaiorCustoComAMargemDoGrupoDeCadaLivro() {
        Map<Long, Dinheiro> precos = consultarPrecos.executar(List.of(1L, 2L));

        assertThat(precos)
                .containsEntry(1L, Dinheiro.de("47.88")) // 39.90 × 1.20
                .containsEntry(2L, Dinheiro.de("64.13")); // 47.50 × 1.35 = 64.125 → 64.13
    }

    @Test
    void rnEst002_livroSemEntradaNaoTemPreco() {
        Map<Long, Dinheiro> precos = consultarPrecos.executar(List.of(20L));

        assertThat(precos).doesNotContainKey(20L);
        assertThat(consultarPrecos.executar(20L)).isEmpty();
    }

    @Test
    void rfEst002_consultaDeUmLivroDevolveOPreco() {
        Optional<Dinheiro> preco = consultarPrecos.executar(2L);

        assertThat(preco).contains(Dinheiro.de("64.13"));
    }

    @Test
    void rfEst002_consultaVaziaNaoAcionaAsPortas() {
        assertThat(consultarPrecos.executar(List.of())).isEmpty();

        assertThat(margens.chamadas).isZero();
        assertThat(custos.chamadas).isZero();
    }

    private static final class MargemPorLivroFake implements MargemPorLivro {

        private final Map<Long, BigDecimal> valores;
        private int chamadas;

        private MargemPorLivroFake(Map<Long, BigDecimal> valores) {
            this.valores = valores;
        }

        @Override
        public Map<Long, BigDecimal> buscar(Collection<Long> livroIds) {
            chamadas++;
            return valores;
        }
    }

    private static final class MaiorCustoPorLivroFake implements MaiorCustoPorLivro {

        private final Map<Long, BigDecimal> valores;
        private int chamadas;

        private MaiorCustoPorLivroFake(Map<Long, BigDecimal> valores) {
            this.valores = valores;
        }

        @Override
        public Map<Long, BigDecimal> buscar(Collection<Long> livroIds) {
            chamadas++;
            return valores;
        }
    }
}
