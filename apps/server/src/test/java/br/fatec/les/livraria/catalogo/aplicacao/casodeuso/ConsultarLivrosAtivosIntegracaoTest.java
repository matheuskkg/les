package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

/**
 * Livros ativos por lote de ids sobre o seed em H2 (RNF-REC-001, RNF-GER-001): quem precisa de poucos livros não
 * lê a vitrine inteira, e o resultado por id é o mesmo da vitrine, com autores, categorias, editora, preço e
 * disponível.
 */
@SpringBootTest
@Transactional
class ConsultarLivrosAtivosIntegracaoTest {

    private static final FiltroLivro SEM_FILTRO = new FiltroLivro(null, null, List.of(), null, null, null, null,
            null, null, null, null, null, null);

    @Autowired
    private ConsultarLivrosAtivos consultarLivrosAtivos;

    @Autowired
    private ListarLivros listarLivros;

    @Test
    void rnfRec001_devolveSoOsAtivosEntreOsIdsComPrecoEDisponivel() {
        // O 19 é inativo no seed e o 9999 não existe; nenhum dos dois pode aparecer.
        Map<Long, LivroListado> livros = consultarLivrosAtivos.executar(List.of(1L, 2L, 19L, 9999L));

        assertThat(livros).containsOnlyKeys(1L, 2L);
        assertThat(livros.get(1L).livro().autores()).isNotEmpty();
        assertThat(livros.get(1L).livro().categorias()).isNotEmpty();
        assertThat(livros.get(1L).livro().editora()).isNotBlank();
        assertThat(livros.get(1L).precoVenda()).isNotNull();
        assertThat(livros.get(1L).disponivel()).isPositive();
    }

    @Test
    void rnfRec001_oLivroPorIdEOMesmoDaVitrine() {
        Pagina<LivroListado> vitrine = listarLivros.executar(SEM_FILTRO, new Paginacao(0, 100, List.of()));
        Map<Long, LivroListado> porId = vitrine.itens().stream()
                .collect(Collectors.toMap(livro -> livro.livro().id(), Function.identity()));
        List<Long> ids = List.of(1L, 5L, 30L, 56L);

        Map<Long, LivroListado> lidos = consultarLivrosAtivos.executar(ids);

        assertThat(lidos).hasSize(ids.size());
        ids.forEach(id -> assertThat(lidos.get(id)).isEqualTo(porId.get(id)));
    }

    @Test
    void rnfRec001_semIdsDevolveMapaVazio() {
        assertThat(consultarLivrosAtivos.executar(List.of())).isEmpty();
    }
}
