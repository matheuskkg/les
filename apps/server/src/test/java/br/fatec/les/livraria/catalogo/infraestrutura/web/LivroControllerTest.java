package br.fatec.les.livraria.catalogo.infraestrutura.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.infraestrutura.persistencia.ReservaJpa;
import br.fatec.les.livraria.estoque.infraestrutura.persistencia.ReservaJpaRepository;

/**
 * Vitrine pública de livros sobre o seed em H2 (RF-LIV-005, RNF-LIV-001, DEC-07, DEC-15.3).
 *
 * <p>O seed tem 20 livros, ids 1 a 20; o livro 19 está inativo e o 20 está ativo, sem entrada de estoque e
 * sem preço. Os valores de preço saem das margens e dos custos de {@code 001_dominios.sql} e
 * {@code 003_estoque.sql}.
 */
@SpringBootTest
@Transactional
class LivroControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private ReservaJpaRepository reservas;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void rfLiv005_listaNoEnvelopeComOrdemPadraoPorTitulo() throws Exception {
        mvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalItens").value(19))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.itens.length()").value(19))
                .andExpect(jsonPath("$.itens[0].titulo").value("21 Lições para o Século 21"))
                .andExpect(jsonPath("$.itens[0].id").value(16))
                .andExpect(jsonPath("$.itens[0].autores.length()").value(1))
                .andExpect(jsonPath("$.itens[0].categorias.length()").value(2))
                .andExpect(jsonPath("$.itens[0].editora").value("Companhia das Letras"))
                .andExpect(jsonPath("$.itens[0].ano").value(2018));
    }

    @Test
    void rfLiv005_paginaComPaginaETamanhoInformados() throws Exception {
        mvc.perform(get("/api/livros").param("pagina", "1").param("tamanho", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(5))
                .andExpect(jsonPath("$.totalItens").value(19))
                .andExpect(jsonPath("$.totalPaginas").value(4))
                .andExpect(jsonPath("$.itens.length()").value(5));
    }

    @Test
    void rfLiv005_ordenaPorAnoDescendente() throws Exception {
        mvc.perform(get("/api/livros").param("ordem", "ano,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(13))
                .andExpect(jsonPath("$.itens[0].ano").value(2021));
    }

    @Test
    void rfLiv005_ordenaPeloNomeDaEditora() throws Exception {
        mvc.perform(get("/api/livros").param("ordem", "editora,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(1))
                .andExpect(jsonPath("$.itens[0].editora").value("Companhia das Letras"));
    }

    @Test
    void rfLiv005_filtraTituloSemDiferenciarMaiusculasNemAcentos() throws Exception {
        mvc.perform(get("/api/livros").param("titulo", "MAO ESQUERDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(17))
                .andExpect(jsonPath("$.itens[0].titulo").value("A Mão Esquerda da Escuridão"));
    }

    @Test
    void rfLiv005_filtraPeloNomeDoAutor() throws Exception {
        mvc.perform(get("/api/livros").param("autor", "tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.itens[*].id", hasItems(5, 6, 7)));
    }

    @Test
    void rfLiv005_combinaDuasCategoriasComOu() throws Exception {
        mvc.perform(get("/api/livros").param("categoria", "3", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(9))
                .andExpect(jsonPath("$.itens[*].id", hasItems(5, 11, 13, 17)));
    }

    @Test
    void rfLiv005_filtraPeloNomeDaEditora() throws Exception {
        mvc.perform(get("/api/livros").param("editora", "rocco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(7))
                .andExpect(jsonPath("$.itens[0].editora").value("Rocco"));
    }

    @Test
    void rfLiv005_filtraPeloIsbnComparandoSoOsDigitos() throws Exception {
        mvc.perform(get("/api/livros").param("isbn", "978-85-00-01001-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(1));
    }

    @Test
    void rfLiv005_filtraPeloCodigoDeBarras() throws Exception {
        mvc.perform(get("/api/livros").param("codigoBarras", "7891000000014"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(1));
    }

    @Test
    void rfLiv005_filtraPorIntervaloDeAno() throws Exception {
        mvc.perform(get("/api/livros").param("anoDe", "1950").param("anoAte", "1955"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(4))
                .andExpect(jsonPath("$.itens[*].id", hasItems(6, 8, 9, 10)));
    }

    @Test
    void rfLiv005_filtraPorIntervaloDePaginas() throws Exception {
        mvc.perform(get("/api/livros").param("paginasDe", "96").param("paginasAte", "208"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(2))
                .andExpect(jsonPath("$.itens[*].id", hasItems(3, 4)));
    }

    @Test
    void rfLiv005_filtraPelaEdicao() throws Exception {
        mvc.perform(get("/api/livros").param("edicao", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(4));
    }

    @Test
    void rfLiv005_combinaFiltrosComE() throws Exception {
        mvc.perform(get("/api/livros")
                .param("autor", "asimov")
                .param("anoDe", "1950")
                .param("anoAte", "1955"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.itens[*].id", hasItems(8, 9, 10)));
    }

    @Test
    void rfLiv005_ignoraParametrosEmBrancoEEspacos() throws Exception {
        mvc.perform(get("/api/livros").param("titulo", "   ").param("autor", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(19));

        mvc.perform(get("/api/livros").param("titulo", "  casmurro  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(1));
    }

    @Test
    void rfLiv005_intervaloInvertidoResponde400() throws Exception {
        mvc.perform(get("/api/livros").param("anoDe", "2000").param("anoAte", "1900"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.parametro").value("ano"))
                .andExpect(jsonPath("$.detail").value(containsString("intervalo")));
    }

    @Test
    void rfLiv005_campoDeOrdenacaoDesconhecidoResponde400() throws Exception {
        mvc.perform(get("/api/livros").param("ordem", "preco,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.parametro").value("ordem"))
                .andExpect(jsonPath("$.detail").value(containsString("preco")));
    }

    @Test
    void rfLiv005_livroInativoNuncaApareceNaListagemNemNoDetalhe() throws Exception {
        mvc.perform(get("/api/livros").param("codigo", "19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(0))
                .andExpect(jsonPath("$.itens").isEmpty());

        mvc.perform(get("/api/livros/19"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Livro não encontrado."));
    }

    @Test
    void rnfLiv001_codigoDoLivroEhOIdUsadoNaListagemENoDetalhe() throws Exception {
        mvc.perform(get("/api/livros").param("codigo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(1));

        mvc.perform(get("/api/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void rfLiv005_detalheTrazPrecoDisponibilidadeEDadosDoLivro() throws Exception {
        mvc.perform(get("/api/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.autores[0]").value("Machado de Assis"))
                .andExpect(jsonPath("$.categorias[*].nome", hasItems("Romance", "História")))
                .andExpect(jsonPath("$.editora").value("Companhia das Letras"))
                .andExpect(jsonPath("$.ano").value(1899))
                .andExpect(jsonPath("$.precoVenda").value("47.88"))
                .andExpect(jsonPath("$.disponivel").value(20))
                .andExpect(jsonPath("$.edicao").value("1ª edição"))
                .andExpect(jsonPath("$.isbn").value("9788500010019"))
                .andExpect(jsonPath("$.paginas").value(256))
                .andExpect(jsonPath("$.sinopse").value(containsString("Bentinho")))
                .andExpect(jsonPath("$.altura").value(21.0))
                .andExpect(jsonPath("$.largura").value(14.0))
                .andExpect(jsonPath("$.profundidade").value(1.6))
                .andExpect(jsonPath("$.peso").value(0.32))
                .andExpect(jsonPath("$.codigoBarras").value("7891000000014"));
    }

    @Test
    void rfLiv005_detalheDeLivroInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/livros/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Livro não encontrado."));
    }

    @Test
    void dec153_disponibilidadeDescontaReservaAtivaEIgnoraVencida() throws Exception {
        LocalDateTime agora = LocalDateTime.now();
        reservas.saveAllAndFlush(List.of(
                novaReserva(1L, 5, agora.plusDays(1)),
                novaReserva(1L, 3, agora.minusDays(1))));

        mvc.perform(get("/api/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(15));

        mvc.perform(get("/api/livros").param("codigo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].disponivel").value(15));
    }

    @Test
    void dec153_disponibilidadeNuncaFicaNegativa() throws Exception {
        reservas.saveAndFlush(novaReserva(1L, 999, LocalDateTime.now().plusDays(1)));

        mvc.perform(get("/api/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(0));
    }

    @Test
    void livroSemPrecoApareceSemPrecoEComDisponivelZero() throws Exception {
        mvc.perform(get("/api/livros/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.precoVenda").doesNotExist())
                .andExpect(jsonPath("$.disponivel").value(0));

        mvc.perform(get("/api/livros").param("codigo", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].precoVenda").doesNotExist())
                .andExpect(jsonPath("$.itens[0].disponivel").value(0));
    }

    @Test
    void rfLiv005_listaAsCategoriasNoEnvelopePaginadoOrdenadasPorNome() throws Exception {
        mvc.perform(get("/api/livros/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalItens").value(5))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.itens.length()").value(5))
                .andExpect(jsonPath("$.itens[0].id").value(3))
                .andExpect(jsonPath("$.itens[0].nome").value("Fantasia"))
                .andExpect(jsonPath("$.itens[4].nome").value("Tecnologia"));
    }

    @Test
    void rfLiv005_paginaAsCategoriasComTamanhoInformado() throws Exception {
        mvc.perform(get("/api/livros/categorias").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.totalItens").value(5))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].nome").value("Fantasia"))
                .andExpect(jsonPath("$.itens[1].nome").value("Ficção Científica"));

        mvc.perform(get("/api/livros/categorias").param("pagina", "2").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Tecnologia"));
    }

    @Test
    void rfLiv005_campoDeOrdenacaoDeCategoriaDesconhecidoResponde400() throws Exception {
        mvc.perform(get("/api/livros/categorias").param("ordem", "id,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.parametro").value("ordem"))
                .andExpect(jsonPath("$.detail").value(containsString("id")));
    }

    private static ReservaJpa novaReserva(Long livroId, int quantidade, LocalDateTime expiraEm) {
        ReservaJpa reserva = new ReservaJpa();
        reserva.setLivroId(livroId);
        reserva.setQuantidade(quantidade);
        reserva.setExpiraEm(expiraEm);
        reserva.setOrigem(OrigemReserva.CARRINHO);
        reserva.setOrigemId(1L);
        return reserva;
    }
}
