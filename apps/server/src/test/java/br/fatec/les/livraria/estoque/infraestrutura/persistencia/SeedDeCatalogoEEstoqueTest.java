package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.infraestrutura.persistencia.AutorJpa;
import br.fatec.les.livraria.catalogo.infraestrutura.persistencia.AutorJpaRepository;
import br.fatec.les.livraria.catalogo.infraestrutura.persistencia.LivroJpa;
import br.fatec.les.livraria.catalogo.infraestrutura.persistencia.LivroJpaRepository;

/** Seed de domínios, livros e estoque em H2 (RNF-LIV-001, RNF-LIV-002, RN-LIV-001, RN-LIV-002). */
@SpringBootTest(properties = {
        // Banco próprio: os outros contextos de teste usam jdbc:h2:mem:livraria e o create-drop
        // de cada contexto recriaria as tabelas mantendo o registro de seed_execucao.
        "spring.datasource.url=jdbc:h2:mem:seed-catalogo-estoque;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
                + "DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1" })
@Transactional
class SeedDeCatalogoEEstoqueTest {

    @Autowired
    private LivroJpaRepository livros;

    @Autowired
    private AutorJpaRepository autores;

    @Autowired
    private EstoqueJpaRepository estoques;

    @Autowired
    private EntradaEstoqueJpaRepository entradas;

    @Test
    void seedCarregaOsLivrosDoCatalogo() {
        assertThat(livros.count()).isEqualTo(20);

        LivroJpa ativoSemEstoque = livros.findById(20L).orElseThrow();
        assertThat(ativoSemEstoque.isAtivo()).isTrue();
        assertThat(estoques.findByLivroId(20L)).isEmpty();
    }

    @Test
    void livroPodeTerVariasCategoriasEAutores() {
        LivroJpa livro = livros.findById(13L).orElseThrow();

        assertThat(livro.getCategorias()).hasSizeGreaterThan(1);
        assertThat(livro.getAutores()).hasSizeGreaterThan(1);
    }

    @Test
    void estoqueEhASomaDasEntradasDoLivro() {
        int somaDasEntradas = entradas.findByLivroId(2L).stream()
                .mapToInt(EntradaEstoqueJpa::getQuantidade)
                .sum();

        EstoqueJpa saldo = estoques.findByLivroId(2L).orElseThrow();

        assertThat(entradas.findByLivroId(2L)).hasSizeGreaterThan(1);
        assertThat(saldo.getQuantidade()).isEqualTo(somaDasEntradas);
    }

    @Test
    void carregaOSaldoComLockPessimistaDentroDaTransacao() {
        // O teste roda em transação (@Transactional), necessária para o FOR UPDATE do lock pessimista.
        EstoqueJpa saldo = estoques.buscarPorLivroIdComLock(2L).orElseThrow();

        assertThat(saldo.getLivroId()).isEqualTo(2L);
        assertThat(saldo.getQuantidade()).isPositive();
    }

    @Test
    void novoRegistroRecebeIdDaFaixaGeradaPelaSequence() {
        AutorJpa novo = new AutorJpa();
        novo.setNome("Autor de Teste");

        autores.saveAndFlush(novo);

        assertThat(novo.getId()).isGreaterThanOrEqualTo(10000L);
        assertThat(autores.findById(novo.getId())).isPresent();
    }
}
