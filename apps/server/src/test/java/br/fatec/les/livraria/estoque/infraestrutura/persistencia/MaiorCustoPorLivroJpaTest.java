package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Agregação do maior custo por livro no seed em H2 (RN-EST-002, DEC-15.3).
 *
 * <p>Os valores esperados saem de {@code 003_estoque.sql}: livro 2 tem 42.00, 47.50 e 45.00; livro 11 tem 79.90, 84.50
 * e 82.00.
 */
@SpringBootTest
@Transactional
class MaiorCustoPorLivroJpaTest {

    @Autowired private MaiorCustoPorLivroJpa maiorCustoPorLivro;

    @Test
    void rnEst002_usaOMaiorCustoEntreAsVariasEntradasDoLivro() {
        Map<Long, BigDecimal> custos = maiorCustoPorLivro.buscar(List.of(2L, 11L));

        assertThat(custos).containsEntry(2L, new BigDecimal("47.50")).containsEntry(11L, new BigDecimal("84.50"));
    }

    @Test
    void rnEst002_soDevolveOsLivrosSolicitados() {
        Map<Long, BigDecimal> custos = maiorCustoPorLivro.buscar(List.of(1L));

        assertThat(custos).containsOnlyKeys(1L);
        assertThat(custos).containsEntry(1L, new BigDecimal("39.90"));
    }

    @Test
    void livroSemEntradaNaoApareceNoResultado() {
        Map<Long, BigDecimal> custos = maiorCustoPorLivro.buscar(List.of(20L));

        assertThat(custos).isEmpty();
    }
}
