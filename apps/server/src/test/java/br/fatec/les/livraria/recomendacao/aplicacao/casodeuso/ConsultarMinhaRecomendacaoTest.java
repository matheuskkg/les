package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosAtivos;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.LerCatalogo;
import br.fatec.les.livraria.recomendacao.aplicacao.MontarPerfilDoCliente;
import br.fatec.les.livraria.recomendacao.aplicacao.RecuperarCandidatos;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RecomendacoesRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ContarCoCompras;
import br.fatec.les.livraria.recomendacao.dominio.calculo.FiltrarItensRecomendados;
import br.fatec.les.livraria.recomendacao.dominio.calculo.OrdenarPelaPontuacao;
import br.fatec.les.livraria.recomendacao.dominio.calculo.PontuarCandidatos;
import br.fatec.les.livraria.recomendacao.dominio.calculo.RecomendacaoAindaValida;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemRecomendado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarComprasDoCliente;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarComprasRegistradas;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarUnidadesVendidas;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraDeLivro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * "Recomendados para você" (RNF-REC-001, DEC-03): o caminho comum, o da recomendação ainda válida por 24 horas,
 * consulta só as compras do cliente e os livros recomendados por lote de ids (RNF-GER-001); a vitrine inteira, as
 * compras de todos os clientes e as unidades vendidas só entram quando outra recomendação é gerada.
 */
class ConsultarMinhaRecomendacaoTest {

    private static final Long CLIENTE_ID = 1L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 21, 12, 0);

    private final IdentificarCliente identificarCliente = mock(IdentificarCliente.class);
    private final ConsultarComprasDoCliente consultarComprasDoCliente = mock(ConsultarComprasDoCliente.class);
    private final ConsultarComprasRegistradas consultarComprasRegistradas = mock(ConsultarComprasRegistradas.class);
    private final ConsultarUnidadesVendidas consultarUnidadesVendidas = mock(ConsultarUnidadesVendidas.class);
    private final ConsultarLivrosAtivos consultarLivrosAtivos = mock(ConsultarLivrosAtivos.class);
    private final LerCatalogo lerCatalogo = mock(LerCatalogo.class);
    private final RecomendacoesRepositorio recomendacoes = mock(RecomendacoesRepositorio.class);
    private final CadeiaDeProvedores cadeia = mock(CadeiaDeProvedores.class);

    private final ConsultarMinhaRecomendacao recomendacao =
            new ConsultarMinhaRecomendacao(
                    identificarCliente,
                    consultarComprasDoCliente,
                    consultarComprasRegistradas,
                    consultarUnidadesVendidas,
                    consultarLivrosAtivos,
                    lerCatalogo,
                    recomendacoes,
                    new MontarPerfilDoCliente(),
                    new RecuperarCandidatos(new ContarCoCompras(), new PontuarCandidatos()),
                    cadeia,
                    new FiltrarItensRecomendados(),
                    new OrdenarPelaPontuacao(),
                    new RecomendacaoAindaValida(),
                    () -> AGORA);

    @Test
    void rnfGer001_recomendacaoAindaValidaNaoLeOMaterialDeGeracao() {
        when(identificarCliente.executar()).thenReturn(CLIENTE_ID);
        // O cliente comprou o livro 4 depois da geração; o recomendado, o 5, continua de pé.
        when(consultarComprasDoCliente.executar(CLIENTE_ID)).thenReturn(List.of(venda(21L, 4L, 1)));
        when(recomendacoes.maisRecente(CLIENTE_ID))
                .thenReturn(
                        Optional.of(
                                new Recomendacao(
                                        7L,
                                        CLIENTE_ID,
                                        99L,
                                        AGORA.minusHours(1),
                                        1,
                                        List.of(new ItemDaRecomendacao(70L, 5L, 1, "do gosto", null, null)))));
        when(consultarLivrosAtivos.executar(List.of(5L))).thenReturn(Map.of(5L, aVenda(5L)));

        var itens = recomendacao.executar();

        assertThat(itens)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.livro().livro().id()).isEqualTo(5L);
                    assertThat(item.justificativa()).isEqualTo("do gosto");
                });
        // O caminho comum não gera outra recomendação: nada do material completo é lido (RNF-GER-001).
        verify(lerCatalogo, never()).ativos();
        verify(consultarComprasRegistradas, never()).executar();
        verify(consultarUnidadesVendidas, never()).executar();
        verify(cadeia, never()).recomendar(any(), any());
    }

    @Test
    void rnfRec001_semRecomendacaoValidaGeraOutraComOsCandidatosDaIa() {
        when(identificarCliente.executar()).thenReturn(CLIENTE_ID);
        when(consultarComprasDoCliente.executar(CLIENTE_ID)).thenReturn(List.of(venda(21L, 1L, 2)));
        when(recomendacoes.maisRecente(CLIENTE_ID)).thenReturn(Optional.empty());
        when(recomendacoes.avaliacoesDoCliente(CLIENTE_ID)).thenReturn(List.of());
        when(consultarComprasRegistradas.executar()).thenReturn(List.of(new CompraDeLivro(CLIENTE_ID, 1L, 2)));
        when(consultarUnidadesVendidas.executar()).thenReturn(Map.of(2L, 4));
        when(lerCatalogo.ativos()).thenReturn(List.of(aVenda(1L), aVenda(2L)));
        when(cadeia.recomendar(any(), any()))
                .thenReturn(
                        new Atendimento<>(
                                new RespostaDeRecomendacao(List.of(new ItemRecomendado(2L, "do gosto"))),
                                123L,
                                "fake"));
        when(recomendacoes.salvar(any()))
                .thenAnswer(chamada -> {
                    Recomendacao recebida = chamada.getArgument(0);
                    List<ItemDaRecomendacao> comIds = new ArrayList<>();
                    for (ItemDaRecomendacao item : recebida.itens()) {
                        comIds.add(
                                new ItemDaRecomendacao(
                                        7L,
                                        item.livroId(),
                                        item.posicao(),
                                        item.justificativa(),
                                        item.avaliacao(),
                                        item.avaliadaEm()));
                    }
                    return new Recomendacao(
                            7L,
                            recebida.clienteId(),
                            recebida.chamadaIaId(),
                            recebida.geradaEm(),
                            recebida.comprasConsideradas(),
                            comIds);
                });
        when(consultarLivrosAtivos.executar(List.of(2L))).thenReturn(Map.of(2L, aVenda(2L)));

        var itens = recomendacao.executar();

        assertThat(itens)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.itemId()).isEqualTo(7L);
                    assertThat(item.livro().livro().id()).isEqualTo(2L);
                    assertThat(item.justificativa()).isEqualTo("do gosto");
                });
        ArgumentCaptor<Recomendacao> gravada = ArgumentCaptor.forClass(Recomendacao.class);
        verify(recomendacoes).salvar(gravada.capture());
        assertThat(gravada.getValue().chamadaIaId()).isEqualTo(123L);
        assertThat(gravada.getValue().comprasConsideradas()).isEqualTo(1);
    }

    private static LivroListado aVenda(Long id) {
        return new LivroListado(
                new LivroResumo(
                        id,
                        "Livro " + id,
                        List.of("Autor " + id),
                        List.of(new CategoriaDoLivro(1L, "Romance")),
                        "Editora de teste",
                        2020),
                Dinheiro.de(new BigDecimal("29.90")),
                5);
    }

    private static LivroVendido venda(Long pedidoId, Long livroId, int quantidade) {
        return new LivroVendido(
                pedidoId,
                CLIENTE_ID,
                livroId,
                "Livro " + livroId,
                List.of("Autor " + livroId),
                List.of("Romance"),
                quantidade,
                AGORA.minusDays(1));
    }
}
