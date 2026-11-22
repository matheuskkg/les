package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/** RF-EST-004, DEC-15.3: reentrada dos itens da troca que voltam ao estoque. */
class ReentrarEstoqueTest {

    private final Map<Long, Integer> estoque = new HashMap<>(Map.of(1L, 20, 2L, 0));
    private final ReentrarEstoque reentrar = new ReentrarEstoque(new EstoqueEmMemoria(estoque));

    @Test
    void rfEst004_somaAsQuantidadesAoSaldo() {
        reentrar.executar(Map.of(1L, 2, 2L, 1));

        assertThat(estoque).containsEntry(1L, 22).containsEntry(2L, 1);
    }

    @Test
    void rfEst004_livroSemSaldoOuQuantidadeNaoPositivaFalha() {
        assertThatThrownBy(() -> reentrar.executar(Map.of(99L, 1))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> reentrar.executar(Map.of(1L, 0))).isInstanceOf(IllegalStateException.class);
        assertThat(estoque).containsEntry(1L, 20);
    }
}
