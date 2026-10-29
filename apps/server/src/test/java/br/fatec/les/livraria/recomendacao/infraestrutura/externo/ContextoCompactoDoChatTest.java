package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;

/**
 * Contexto enxuto do chat (RNF-REC-001, DEC-03, REC-09): catálogo em uma linha por livro e perfil com ids, sem
 * repetir os dados que já estão no catálogo.
 */
class ContextoCompactoDoChatTest {

    private static final ResumoDeLivro DOM_CASMURRO = new ResumoDeLivro(1L, "Dom Casmurro",
            List.of("Machado de Assis"), List.of("Romance"), Dinheiro.de("47.88"));
    private static final ResumoDeLivro O_CORTICO = new ResumoDeLivro(2L, "O Cortiço",
            List.of("Aluísio Azevedo"), List.of("Romance"), Dinheiro.de("29.90"));

    @Test
    void rnfRec001_catalogoTemCabecalhoEUmaLinhaPorLivro() {
        String catalogo = ContextoCompactoDoChat.catalogo(List.of(DOM_CASMURRO, O_CORTICO));

        assertThat(catalogo).isEqualTo("""
                id|titulo|autores|categorias|preco
                1|Dom Casmurro|Machado de Assis|Romance|47.88
                2|O Cortiço|Aluísio Azevedo|Romance|29.90""");
    }

    @Test
    void rnfRec001_separadorEQuebraDeLinhaNoTituloNaoDesalinhamAsColunas() {
        ResumoDeLivro livro = new ResumoDeLivro(3L, "Contos | Volume 1\nEdição", List.of("Vários"),
                List.of("Contos"), Dinheiro.de("10.00"));

        assertThat(ContextoCompactoDoChat.catalogo(List.of(livro)))
                .endsWith("\n3|Contos Volume 1 Edição|Vários|Contos|10.00");
    }

    @Test
    void rnfRec001_catalogoVazioTemSoOCabecalho() {
        assertThat(ContextoCompactoDoChat.catalogo(List.of())).isEqualTo("id|titulo|autores|categorias|preco\n");
    }

    @Test
    void rnfRec001_perfilUsaIdDoLivroNoCatalogoETituloDosDemais() {
        PerfilDoCliente perfil = new PerfilDoCliente(
                List.of(new LivroComprado(1L, "Dom Casmurro", List.of("Romance"), List.of("Machado de Assis")),
                        new LivroComprado(99L, "Livro Antigo", List.of("Conto"), List.of("Autor Antigo"))),
                List.of("Romance"),
                List.of("Machado de Assis"),
                List.of(new AvaliacaoDada(1L, true), new AvaliacaoDada(2L, false)));

        String descricao = ContextoCompactoDoChat.perfil(perfil, List.of(DOM_CASMURRO, O_CORTICO));

        assertThat(descricao).isEqualTo("""
                - Livros comprados: 1, "Livro Antigo"
                - Categorias frequentes: Romance
                - Autores frequentes: Machado de Assis
                - Avaliações de recomendações: positivas: 1; negativas: 2""");
    }

    @Test
    void rnfRec001_perfilLimitaCategoriasEAutoresAosCincoMaisFrequentes() {
        PerfilDoCliente perfil = new PerfilDoCliente(List.of(),
                List.of("Romance", "Conto", "Poesia", "Teatro", "Crônica", "Biografia"),
                List.of("Machado de Assis", "Aluísio Azevedo", "Clarice Lispector", "Graciliano Ramos",
                        "Jorge Amado", "Cecília Meireles"),
                List.of());

        String descricao = ContextoCompactoDoChat.perfil(perfil, List.of());

        assertThat(descricao)
                .contains("- Categorias frequentes: Romance, Conto, Poesia, Teatro, Crônica\n")
                .contains("- Autores frequentes: Machado de Assis, Aluísio Azevedo, Clarice Lispector, Graciliano "
                        + "Ramos, Jorge Amado\n")
                .doesNotContain("Biografia")
                .doesNotContain("Cecília Meireles");
    }

    @Test
    void rnfRec001_perfilVazioNaoRepeteTitulos() {
        PerfilDoCliente perfil = new PerfilDoCliente(List.of(), List.of(), List.of(), List.of());

        String descricao = ContextoCompactoDoChat.perfil(perfil, List.of(DOM_CASMURRO));

        assertThat(descricao).isEqualTo("""
                - Livros comprados: nenhum
                - Categorias frequentes: nenhum
                - Autores frequentes: nenhum
                - Avaliações de recomendações: nenhuma""");
    }
}
