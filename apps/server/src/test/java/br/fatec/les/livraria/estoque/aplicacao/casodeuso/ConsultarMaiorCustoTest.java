package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;

/** Maior custo por livro em lote (RN-EST-002, DEC-15.3). */
class ConsultarMaiorCustoTest {

    @Test
    void rnEst002_devolveOMaiorCustoDeCadaLivro() {
        MaiorCustoPorLivroFake porta = new MaiorCustoPorLivroFake(Map.of(
                1L, new BigDecimal("39.90"),
                2L, new BigDecimal("47.50")));
        ConsultarMaiorCusto consultarMaiorCusto = new ConsultarMaiorCusto(porta);

        Map<Long, BigDecimal> resultado = consultarMaiorCusto.executar(List.of(1L, 2L));

        assertThat(resultado).containsEntry(1L, new BigDecimal("39.90"));
        assertThat(resultado).containsEntry(2L, new BigDecimal("47.50"));
        assertThat(porta.chamadas).isEqualTo(1);
    }

    @Test
    void rnEst002_consultaVaziaNaoAcionaAPorta() {
        MaiorCustoPorLivroFake porta = new MaiorCustoPorLivroFake(Map.of());
        ConsultarMaiorCusto consultarMaiorCusto = new ConsultarMaiorCusto(porta);

        assertThat(consultarMaiorCusto.executar(List.of())).isEmpty();
        assertThat(porta.chamadas).isZero();
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
