package br.fatec.les.livraria.vendas.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import jakarta.servlet.http.Cookie;

/**
 * Carrinho do cliente sobre o seed (RF-VEN-001, RF-VEN-002, RN-VEN-001, DEC-15.3): livro 1 (Dom Casmurro, 20 em
 * estoque, R$ 47,88), livro 7 (O Silmarillion, 6 em estoque) e livro 20 (Persuasão, sem preço nem estoque). Ana é o
 * usuário 2 e Bruno o 3. O relógio é ajustável para testar a reserva vencida.
 */
@SpringBootTest
@Transactional
class CarrinhoIntegracaoTest {

    private static final long ANA = 2L;
    private static final long BRUNO = 3L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>(INICIO);

    @TestConfiguration
    static class RelogioAjustavelDoTeste {

        @Bean
        @Primary
        Relogio relogioDoTeste() {
            return AGORA::get;
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(INICIO);
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfVen001_carrinhoNovoEstaVazio() throws Exception {
        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(0)))
                .andExpect(jsonPath("$.subtotal").value("0.00"))
                .andExpect(jsonPath("$.expiraEm").doesNotExist());
    }

    @Test
    void rfVen001_adicionaSomaNaMesmaLinhaECalculaOsTotais() throws Exception {
        adicionar(ANA, 1, 2).andExpect(status().isOk());
        adicionar(ANA, 1, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value("47.88"))
                .andExpect(jsonPath("$.itens[0].subtotal").value("143.64"))
                .andExpect(jsonPath("$.subtotal").value("143.64"))
                .andExpect(jsonPath("$.quantidadeDeItens").value(3))
                .andExpect(jsonPath("$.expiraEm").exists());
    }

    @Test
    void rfVen002_alteraEDepoisRemoveOItem() throws Exception {
        adicionar(ANA, 1, 2).andExpect(status().isOk());
        escrever(put("/api/me/carrinho/itens/1"), ANA, "{\"quantidade\":5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].quantidade").value(5));
        escrever(delete("/api/me/carrinho/itens/1"), ANA, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(0)));
    }

    @Test
    void rfVen002_quantidadeZeroOuItemForaDoCarrinho() throws Exception {
        adicionar(ANA, 1, 1).andExpect(status().isOk());
        escrever(put("/api/me/carrinho/itens/1"), ANA, "{\"quantidade\":0}")
                .andExpect(status().isUnprocessableContent());
        escrever(put("/api/me/carrinho/itens/5"), ANA, "{\"quantidade\":1}")
                .andExpect(status().isNotFound());
    }

    @Test
    void rnVen001_acimaDoDisponivelResponde422SemAlterarOCarrinho() throws Exception {
        adicionar(ANA, 7, 4).andExpect(status().isOk());
        adicionar(ANA, 7, 3)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-VEN-001")));

        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$.itens[0].quantidade").value(4));
    }

    @Test
    void rnVen001_reservaDeUmClienteReduzODisponivelDoOutro() throws Exception {
        adicionar(ANA, 7, 5).andExpect(status().isOk());
        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(1));

        adicionar(BRUNO, 7, 2).andExpect(status().isUnprocessableContent());
        adicionar(BRUNO, 7, 1).andExpect(status().isOk());
    }

    @Test
    void rnVen014_reservaVencidaDeixaDeConsumirEstoque() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(0));

        AGORA.set(INICIO.plusHours(1));

        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(6));
        adicionar(BRUNO, 7, 6).andExpect(status().isOk());
    }

    @Test
    void rnVen001_renovarOPrazoNaoRessuscitaReservaVencidaSemEstoque() throws Exception {
        adicionar(ANA, 7, 6).andExpect(status().isOk());
        AGORA.set(INICIO.plusHours(1));
        adicionar(BRUNO, 7, 6).andExpect(status().isOk());

        adicionar(ANA, 1, 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-VEN-001")));
        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(0));
    }

    @Test
    void rnVen014_alterarComPrazoVencidoReservaDeNovoTodoOCarrinho() throws Exception {
        adicionar(ANA, 1, 1).andExpect(status().isOk());
        adicionar(ANA, 7, 2).andExpect(status().isOk());
        AGORA.set(INICIO.plusHours(1));

        escrever(put("/api/me/carrinho/itens/1"), ANA, "{\"quantidade\":2}").andExpect(status().isOk());

        mvc.perform(get("/api/livros/7")).andExpect(jsonPath("$.disponivel").value(4));
    }

    @Test
    void rfVen001_livroSemPrecoOuInexistenteResponde422() throws Exception {
        adicionar(ANA, 20, 1).andExpect(status().isUnprocessableContent());
        adicionar(ANA, 999_999, 1).andExpect(status().isUnprocessableContent());
    }

    @Test
    void dec15_4_carrinhoExigeSessaoDeCliente() throws Exception {
        mvc.perform(get("/api/me/carrinho")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me/carrinho").cookie(sessaoDe(1L))).andExpect(status().isForbidden());
    }

    private ResultActions adicionar(long usuarioId, long livroId, int quantidade) throws Exception {
        return escrever(post("/api/me/carrinho/itens"), usuarioId,
                "{\"livroId\":%d,\"quantidade\":%d}".formatted(livroId, quantidade));
    }

    private ResultActions escrever(MockHttpServletRequestBuilder requisicao, long usuarioId, String corpo)
            throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
        MockHttpServletRequestBuilder comSessao = requisicao.cookie(sessaoDe(usuarioId), csrf)
                .header("X-XSRF-TOKEN", csrf.getValue());
        return mvc.perform(corpo.isEmpty() ? comSessao
                : comSessao.contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Cookie sessaoDe(long usuarioId) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
