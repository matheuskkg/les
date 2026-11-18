package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Preço de venda calculado sobre o seed em H2 (RF-EST-002, RN-EST-002, RN-LIV-003, DEC-15.1).
 *
 * <p>Os valores esperados saem das margens do seed (Padrão 0.2000, Popular 0.3500, Premium 0.5000) e do
 * maior custo de cada livro em {@code 003_estoque.sql}.
 */
@SpringBootTest
@Transactional
class ConsultarPrecosDeVendaIntegracaoTest {

    private static final List<Long> TODOS_OS_LIVROS = IntStream.rangeClosed(1, 26)
            .mapToObj(Long::valueOf)
            .toList();

    @Autowired
    private ConsultarPrecosDeVenda consultarPrecosDeVenda;

    @Test
    void rfEst002_calculaPrecoPeloMaiorCustoEntreAsEntradasDoLivro() {
        // Livro 2 (Popular, 35%): entradas de 42.00, 47.50 e 45.00 → 47.50 × 1.35 = 64.125 → 64.13.
        assertThat(consultarPrecosDeVenda.executar(List.of(2L))).containsEntry(2L, Dinheiro.de("64.13"));
    }

    @Test
    void livroSemEntradasNaoTemPreco() {
        Map<Long, Dinheiro> precos = consultarPrecosDeVenda.executar(List.of(20L));

        assertThat(precos).doesNotContainKey(20L);
    }

    @Test
    void rfEst002_calculaPrecoDeTodosOsLivrosComEntradaDoSeed() {
        Map<Long, Dinheiro> precos = consultarPrecosDeVenda.executar(TODOS_OS_LIVROS);

        assertThat(precos).hasSize(25).doesNotContainKey(20L);
        assertThat(precos)
                .containsEntry(1L, Dinheiro.de("47.88"))
                .containsEntry(2L, Dinheiro.de("64.13"))
                .containsEntry(3L, Dinheiro.de("40.37"))
                .containsEntry(4L, Dinheiro.de("43.80"))
                .containsEntry(5L, Dinheiro.de("42.90"))
                .containsEntry(6L, Dinheiro.de("59.88"))
                .containsEntry(7L, Dinheiro.de("72.90"))
                .containsEntry(8L, Dinheiro.de("49.44"))
                .containsEntry(9L, Dinheiro.de("44.96"))
                .containsEntry(10L, Dinheiro.de("44.88"))
                .containsEntry(11L, Dinheiro.de("126.75"))
                .containsEntry(12L, Dinheiro.de("134.85"))
                .containsEntry(13L, Dinheiro.de("107.25"))
                .containsEntry(14L, Dinheiro.de("56.28"))
                .containsEntry(15L, Dinheiro.de("59.40"))
                .containsEntry(16L, Dinheiro.de("59.94"))
                .containsEntry(17L, Dinheiro.de("47.88"))
                .containsEntry(18L, Dinheiro.de("56.97"))
                .containsEntry(19L, Dinheiro.de("78.30"))
                // Livros novos: maior custo entre as duas entradas × margem do grupo (RN-EST-002, DEC-15.1).
                .containsEntry(21L, Dinheiro.de("49.20"))
                .containsEntry(22L, Dinheiro.de("51.30"))
                .containsEntry(23L, Dinheiro.de("78.00"))
                .containsEntry(24L, Dinheiro.de("48.60"))
                .containsEntry(25L, Dinheiro.de("69.60"))
                .containsEntry(26L, Dinheiro.de("69.00"));
    }
}
