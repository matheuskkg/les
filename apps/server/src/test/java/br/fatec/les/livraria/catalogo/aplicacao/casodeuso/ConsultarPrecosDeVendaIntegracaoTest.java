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
 * <p>Os valores esperados saem das margens do seed (Padrão 0.2000, Popular 0.3500, Premium 0.5000, Econômico
 * 0.1500 e Colecionador 0.7000) e do maior custo de cada livro nos scripts de estoque.
 */
@SpringBootTest
@Transactional
class ConsultarPrecosDeVendaIntegracaoTest {

    private static final List<Long> TODOS_OS_LIVROS = IntStream.rangeClosed(1, 56)
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

        assertThat(precos).hasSize(55).doesNotContainKey(20L);
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
                .containsEntry(26L, Dinheiro.de("69.00"))
                // ALT-02: dois grupos novos (Econômico 15% e Colecionador 70%) e quatro livros com duas entradas,
                // em que vale o maior custo (RN-EST-002).
                .containsEntry(27L, Dinheiro.de("66.00"))
                .containsEntry(28L, Dinheiro.de("43.88"))
                .containsEntry(29L, Dinheiro.de("57.00"))
                .containsEntry(30L, Dinheiro.de("48.47"))
                .containsEntry(31L, Dinheiro.de("33.60"))
                .containsEntry(32L, Dinheiro.de("55.35"))
                .containsEntry(33L, Dinheiro.de("25.30"))
                .containsEntry(34L, Dinheiro.de("63.45"))
                .containsEntry(35L, Dinheiro.de("82.50"))
                .containsEntry(36L, Dinheiro.de("151.30"))
                .containsEntry(37L, Dinheiro.de("50.63"))
                .containsEntry(38L, Dinheiro.de("62.10"))
                .containsEntry(39L, Dinheiro.de("54.00"))
                .containsEntry(40L, Dinheiro.de("52.65"))
                .containsEntry(41L, Dinheiro.de("27.60"))
                .containsEntry(42L, Dinheiro.de("45.23"))
                .containsEntry(43L, Dinheiro.de("29.90"))
                .containsEntry(44L, Dinheiro.de("66.15"))
                .containsEntry(45L, Dinheiro.de("87.00"))
                .containsEntry(46L, Dinheiro.de("156.40"))
                .containsEntry(47L, Dinheiro.de("51.30"))
                .containsEntry(48L, Dinheiro.de("54.00"))
                .containsEntry(49L, Dinheiro.de("63.00"))
                .containsEntry(50L, Dinheiro.de("65.48"))
                .containsEntry(51L, Dinheiro.de("33.35"))
                .containsEntry(52L, Dinheiro.de("46.58"))
                .containsEntry(53L, Dinheiro.de("55.50"))
                .containsEntry(54L, Dinheiro.de("56.03"))
                .containsEntry(55L, Dinheiro.de("62.10"))
                .containsEntry(56L, Dinheiro.de("59.25"));
    }
}
