package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Consulta em lote do maior custo por livro, delegando a agregação à porta (DEC-15.3). */
class ConsultarMaiorCustoTest {

    @Test
    void devolveOsCustosInformadosPelaPorta() {
        MaiorCustoPorLivroFake porta =
                new MaiorCustoPorLivroFake(
                        Map.of(
                                1L, new BigDecimal("39.90"),
                                2L, new BigDecimal("47.50")));
        ConsultarMaiorCusto consultarMaiorCusto = new ConsultarMaiorCusto(porta);

        Map<Long, BigDecimal> resultado = consultarMaiorCusto.executar(List.of(1L, 2L));

        assertThat(resultado).containsEntry(1L, new BigDecimal("39.90"));
        assertThat(resultado).containsEntry(2L, new BigDecimal("47.50"));
        assertThat(porta.chamadas).isEqualTo(1);
        assertThat(porta.idsRecebidos).containsExactly(1L, 2L);
    }

    @Test
    void consultaVaziaNaoAcionaAPorta() {
        MaiorCustoPorLivroFake porta = new MaiorCustoPorLivroFake(Map.of());
        ConsultarMaiorCusto consultarMaiorCusto = new ConsultarMaiorCusto(porta);

        assertThat(consultarMaiorCusto.executar(List.of())).isEmpty();
        assertThat(porta.chamadas).isZero();
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
