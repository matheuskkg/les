package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Seed do catálogo em H2 (RNF-LIV-001, RNF-LIV-002, RN-LIV-001). */
@SpringBootTest
@Transactional
class SeedDeCatalogoTest {

    @Autowired
    private LivroJpaRepository livros;

    @Autowired
    private AutorJpaRepository autores;

    @Test
    void seedCarregaOsLivrosDoCatalogo() {
        assertThat(livros.count()).isEqualTo(20);

        LivroJpa ativoSemEstoque = livros.findById(20L).orElseThrow();
        assertThat(ativoSemEstoque.isAtivo()).isTrue();
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
