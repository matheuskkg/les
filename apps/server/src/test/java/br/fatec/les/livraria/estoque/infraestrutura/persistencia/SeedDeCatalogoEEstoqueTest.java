package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Seed de estoque em H2 (RNF-LIV-002, RN-LIV-001, DEC-15.3). */
@SpringBootTest
@Transactional
class SeedDeCatalogoEEstoqueTest {

    @Autowired
    private EstoqueJpaRepository estoques;

    @Autowired
    private EntradaEstoqueJpaRepository entradas;

    @Test
    void livroSemEntradasTemSaldoZero() {
        assertThat(entradas.findByLivroId(20L)).isEmpty();

        EstoqueJpa saldo = estoques.findByLivroId(20L).orElseThrow();

        assertThat(saldo.getQuantidade()).isZero();
    }

    @Test
    void estoqueEhASomaDasEntradasDoLivro() {
        List<EntradaEstoqueJpa> entradasDoLivro = entradas.findByLivroId(2L);
        int somaDasEntradas = entradasDoLivro.stream()
                .mapToInt(EntradaEstoqueJpa::getQuantidade)
                .sum();

        EstoqueJpa saldo = estoques.findByLivroId(2L).orElseThrow();

        assertThat(entradasDoLivro).hasSizeGreaterThan(1);
        assertThat(saldo.getQuantidade()).isEqualTo(somaDasEntradas);
    }

    @Test
    void carregaOSaldoComLockPessimistaDentroDaTransacao() {
        // O teste roda em transação (@Transactional), necessária para o FOR UPDATE do lock pessimista.
        EstoqueJpa saldo = estoques.buscarPorLivroIdComLock(2L).orElseThrow();

        assertThat(saldo.getLivroId()).isEqualTo(2L);
        assertThat(saldo.getQuantidade()).isPositive();
    }
}
