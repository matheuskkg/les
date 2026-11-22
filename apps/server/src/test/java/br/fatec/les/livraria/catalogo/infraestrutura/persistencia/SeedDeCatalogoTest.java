package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.estoque.infraestrutura.persistencia.FornecedorJpaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Seed do catálogo em H2 (RNF-LIV-001, RNF-LIV-002, RN-LIV-001). */
@SpringBootTest
@Transactional
class SeedDeCatalogoTest {

    @Autowired private LivroJpaRepository livros;

    @Autowired private AutorJpaRepository autores;

    @Autowired private EditoraJpaRepository editoras;

    @Autowired private CategoriaJpaRepository categorias;

    @Autowired private GrupoPrecificacaoJpaRepository grupos;

    @Autowired private FornecedorJpaRepository fornecedores;

    @Test
    void seedCarregaOsLivrosDoCatalogo() {
        assertThat(livros.count()).isEqualTo(56);

        LivroJpa ativoSemEstoque = livros.findById(20L).orElseThrow();
        assertThat(ativoSemEstoque.isAtivo()).isTrue();

        // Os livros 21 a 26 e os 30 acrescentados depois (27 a 56) ampliam o catálogo do seed (RNF-LIV-002).
        for (long id = 21; id <= 56; id++) {
            LivroJpa livro = livros.findById(id).orElseThrow();
            assertThat(livro.isAtivo()).isTrue();
            assertThat(livro.getAutores()).isNotEmpty();
            assertThat(livro.getCategorias()).isNotEmpty();
        }
    }

    @Test
    void rnfLiv002_seedTemVariedadeDeDominiosParaRecomendacoes() {
        assertThat(autores.count()).isEqualTo(29);
        assertThat(editoras.count()).isEqualTo(15);
        assertThat(categorias.count()).isEqualTo(17);
        assertThat(grupos.count()).isEqualTo(5);
        assertThat(fornecedores.count()).isEqualTo(10);

        assertThat(grupos.findById(4L).orElseThrow().getMargem()).isEqualByComparingTo("0.1500");
        assertThat(grupos.findById(5L).orElseThrow().getMargem()).isEqualByComparingTo("0.7000");
    }

    @Test
    void rnfLiv001_todosOsLivrosTemIsbnEcodigoDeBarrasUnicos() {
        List<LivroJpa> todos = livros.findAll();

        assertThat(todos).hasSize(56);
        assertThat(todos.stream().map(LivroJpa::getIsbn).distinct()).hasSize(56);
        assertThat(todos.stream().map(LivroJpa::getCodigoBarras).distinct()).hasSize(56);
    }

    @Test
    void livroPodeTerVariasCategoriasEAutores() {
        LivroJpa livro = livros.findById(13L).orElseThrow();

        assertThat(livro.getCategorias()).hasSizeGreaterThan(1);
        assertThat(livro.getAutores()).hasSizeGreaterThan(1);
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
