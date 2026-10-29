package br.fatec.les.livraria.recomendacao.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarSinopses;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RecomendacoesRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ContarCoCompras;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ContarVendidos;
import br.fatec.les.livraria.recomendacao.dominio.calculo.PontuarCandidatos;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarLivrosVendidos;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;

/**
 * Tool {@code recomendarParaMim} com a recuperação da vitrine (RNF-REC-001, DEC-03): sem comprados, sem avaliados
 * negativamente e, sem histórico, os mais vendidos.
 */
class FerramentasDoCatalogoTest {

    private static final Long CLIENTE_ID = 10L;
    private static final Long OUTRO_CLIENTE_ID = 20L;

    private final LerCatalogo lerCatalogo = mock(LerCatalogo.class);
    private final ConsultarSinopses consultarSinopses = mock(ConsultarSinopses.class);
    private final ConsultarLivrosVendidos consultarLivrosVendidos = mock(ConsultarLivrosVendidos.class);
    private final RecomendacoesRepositorio recomendacoes = mock(RecomendacoesRepositorio.class);
    private final FerramentasDoCatalogo ferramentas = new FerramentasDoCatalogo(lerCatalogo, consultarSinopses,
            consultarLivrosVendidos, new MontarPerfilDoCliente(),
            new RecuperarCandidatos(new ContarCoCompras(), new ContarVendidos(), new PontuarCandidatos()),
            recomendacoes);

    @Test
    void rnfRec001_recomendarParaMimExcluiCompradosEAvaliadosNegativamente() {
        LivroListado comprado = livro(1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"));
        LivroListado doGosto = livro(2L, "Memórias Póstumas", List.of("Machado de Assis"), List.of("Romance"));
        LivroListado recusado = livro(3L, "O Cortiço", List.of("Aluísio Azevedo"), List.of("Naturalismo"));
        LivroListado outro = livro(4L, "Capitães da Areia", List.of("Jorge Amado"), List.of("Modernismo"));
        when(lerCatalogo.ativos()).thenReturn(List.of(comprado, doGosto, recusado, outro));
        when(consultarLivrosVendidos.executar()).thenReturn(List.of(
                venda(1L, CLIENTE_ID, comprado, 1),
                venda(2L, OUTRO_CLIENTE_ID, outro, 1)));
        when(recomendacoes.avaliacoesDoCliente(CLIENTE_ID)).thenReturn(List.of(new AvaliacaoDada(3L, false)));

        List<ResumoDeLivro> recomendados = ferramentas.doCliente(CLIENTE_ID).recomendarParaMim();

        assertThat(recomendados).extracting(ResumoDeLivro::id).containsExactly(2L, 4L);
    }

    @Test
    void rnfRec001_semHistoricoRecomendarParaMimUsaOsMaisVendidos() {
        LivroListado maisVendido = livro(1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"));
        LivroListado segundo = livro(2L, "Memórias Póstumas", List.of("Machado de Assis"), List.of("Romance"));
        LivroListado terceiro = livro(3L, "O Cortiço", List.of("Aluísio Azevedo"), List.of("Naturalismo"));
        when(lerCatalogo.ativos()).thenReturn(List.of(segundo, terceiro, maisVendido));
        when(consultarLivrosVendidos.executar()).thenReturn(List.of(
                venda(1L, OUTRO_CLIENTE_ID, maisVendido, 10),
                venda(2L, OUTRO_CLIENTE_ID, segundo, 3),
                venda(3L, OUTRO_CLIENTE_ID, terceiro, 1)));
        when(recomendacoes.avaliacoesDoCliente(CLIENTE_ID)).thenReturn(List.of());

        List<ResumoDeLivro> recomendados = ferramentas.doCliente(CLIENTE_ID).recomendarParaMim();

        assertThat(recomendados).extracting(ResumoDeLivro::id).containsExactly(1L, 2L, 3L);
    }

    @Test
    void rnfRec001_recomendarParaMimLeOCatalogoEOsVendidosUmaVezPorMensagem() {
        LivroListado livro = livro(1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"));
        when(lerCatalogo.ativos()).thenReturn(List.of(livro));
        when(consultarLivrosVendidos.executar()).thenReturn(List.of());
        when(recomendacoes.avaliacoesDoCliente(CLIENTE_ID)).thenReturn(List.of());
        var doCliente = ferramentas.doCliente(CLIENTE_ID);

        doCliente.recomendarParaMim();
        doCliente.recomendarParaMim();

        verify(lerCatalogo, times(1)).ativos();
        verify(consultarLivrosVendidos, times(1)).executar();
    }

    private static LivroListado livro(Long id, String titulo, List<String> autores, List<String> categorias) {
        List<CategoriaDoLivro> categoriasDoLivro = new ArrayList<>();
        for (int i = 0; i < categorias.size(); i++) {
            categoriasDoLivro.add(new CategoriaDoLivro((long) i + 1, categorias.get(i)));
        }
        return new LivroListado(new LivroResumo(id, titulo, autores, categoriasDoLivro, "Editora de teste", 2020),
                Dinheiro.de(new BigDecimal("29.90")), 5);
    }

    private static LivroVendido venda(Long pedidoId, Long clienteId, LivroListado livro, int quantidade) {
        return new LivroVendido(pedidoId, clienteId, livro.livro().id(), livro.livro().titulo(),
                livro.livro().autores(), livro.livro().categorias().stream().map(CategoriaDoLivro::nome).toList(),
                quantidade, LocalDateTime.of(2026, 1, 1, 12, 0).plusDays(pedidoId));
    }
}
