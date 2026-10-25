package br.fatec.les.livraria.recomendacao.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.CadeiaDeProvedores;
import jakarta.servlet.http.Cookie;

/**
 * "Recomendados para você" sobre o seed, atendido pelo provedor {@code fake} (RNF-REC-001, DEC-03). Os clientes
 * ativos com histórico no seed já compraram quase todo o catálogo, então o teste copia para Ana (usuário 2,
 * cliente 1) o pedido entregue 2 do seed, dentro da transação do teste; Bruno (usuário 3) não comprou e recebe os
 * mais vendidos.
 */
@SpringBootTest(properties = {
        "livraria.ia.provedores.gemini.chave=",
        "livraria.ia.provedores.groq.chave=",
        "livraria.ia.provedores.openrouter.chave="})
@Transactional
class RecomendacaoIntegracaoTest {

    private static final Long ANA = 2L;
    private static final Long BRUNO = 3L;
    private static final Long ADMIN = 1L;
    private static final AtomicReference<LocalDateTime> AGORA = new AtomicReference<>();

    @TestConfiguration
    static class RelogioAjustavel {

        @Bean
        @Primary
        Relogio relogioAjustavel() {
            return AGORA::get;
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CadeiaDeProvedores cadeia;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        AGORA.set(LocalDateTime.of(2026, 10, 25, 12, 0));
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        cadeia.limpar();
        copiar("pedido", "id = 2", "id", "900000", "cliente_id", "1");
        copiar("item_pedido", "pedido_id = 2", "id", "id + 900000", "pedido_id", "900000");
    }

    /** Copia linhas trocando as colunas informadas por expressões SQL (pares coluna, expressão). */
    private void copiar(String tabela, String filtro, String... trocas) {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        List<String> colunas = jdbc.sql("select column_name from information_schema.columns "
                + "where table_schema = 'public' and table_name = :tabela order by ordinal_position")
                .param("tabela", tabela).query(String.class).list();
        java.util.Map<String, String> expressoes = new java.util.HashMap<>();
        for (int i = 0; i < trocas.length; i += 2) {
            expressoes.put(trocas[i], trocas[i + 1]);
        }
        String destino = colunas.stream().map(coluna -> "\"" + coluna + "\"")
                .collect(java.util.stream.Collectors.joining(", "));
        String origem = colunas.stream().map(coluna -> expressoes.getOrDefault(coluna, "\"" + coluna + "\""))
                .collect(java.util.stream.Collectors.joining(", "));
        jdbc.sql("insert into \"" + tabela + "\" (" + destino + ") select " + origem + " from \"" + tabela
                + "\" where " + filtro).update();
    }

    @AfterTransaction
    void limparChamadas() {
        // As chamadas são gravadas em transação própria e não somem com o rollback do teste.
        JdbcClient.create(dataSource).sql("delete from chamada_ia").update();
    }

    private MockHttpServletRequestBuilder como(Long usuarioId, MockHttpServletRequestBuilder requisicao) {
        String token = emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor();
        return requisicao.cookie(new Cookie(EmissorDeToken.COOKIE, token)).with(csrf());
    }

    private List<Integer> idsDosItens(Long usuarioId) throws Exception {
        String corpo = mvc.perform(como(usuarioId, get("/api/me/recomendacoes")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$[*].id");
    }

    private List<Integer> idsDosLivros(Long usuarioId) throws Exception {
        String corpo = mvc.perform(como(usuarioId, get("/api/me/recomendacoes")))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$[*].livro.id");
    }

    private long chamadas() {
        return JdbcClient.create(dataSource).sql("select count(*) from chamada_ia").query(Long.class).single();
    }

    private void avaliar(Long usuarioId, Integer itemId, String avaliacao) throws Exception {
        mvc.perform(como(usuarioId, put("/api/me/recomendacoes/itens/{id}/avaliacao", itemId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avaliacao\":\"" + avaliacao + "\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rnfRec001_clienteComHistoricoRecebeLivrosJustificadosPelaIa() throws Exception {
        mvc.perform(como(ANA, get("/api/me/recomendacoes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.greaterThan(0), org.hamcrest.Matchers.lessThanOrEqualTo(8))))
                .andExpect(jsonPath("$[0].justificativa")
                        .value("Indicado pelas categorias e autores que você costuma comprar."))
                .andExpect(jsonPath("$[0].livro.titulo").isString())
                .andExpect(jsonPath("$[0].livro.precoVenda").isString())
                .andExpect(jsonPath("$[0].avaliacao").doesNotExist());
        assertThat(chamadas()).isEqualTo(1);
    }

    @Test
    void rnfRec001_semHistoricoRecebeOsMaisVendidos() throws Exception {
        mvc.perform(como(BRUNO, get("/api/me/recomendacoes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].justificativa").value("Entre os mais vendidos da loja."));
    }

    @Test
    void rnfRec001_recomendacaoReaproveitadaPor24Horas() throws Exception {
        List<Integer> primeira = idsDosItens(ANA);
        AGORA.set(AGORA.get().plusHours(23));

        assertThat(idsDosItens(ANA)).isEqualTo(primeira);
        assertThat(chamadas()).isEqualTo(1);

        AGORA.set(AGORA.get().plusHours(2));
        assertThat(idsDosItens(ANA)).doesNotContainAnyElementsOf(primeira);
        assertThat(chamadas()).isEqualTo(2);
    }

    @Test
    void rnfRec001_compraQuePassaAContarComoVendaGeraOutra() throws Exception {
        idsDosItens(BRUNO);
        // Pedido que passou a contar como venda depois da geração, com data anterior a ela.
        copiar("pedido", "id = 2", "id", "900001", "cliente_id", "2");
        copiar("item_pedido", "pedido_id = 2", "id", "id + 900100", "pedido_id", "900001");

        idsDosItens(BRUNO);

        assertThat(chamadas()).isEqualTo(2);
    }

    @Test
    void rnfRec001_avaliadoNegativamenteSomeNaHoraESaiDosCandidatosSeguintes() throws Exception {
        List<Integer> itens = idsDosItens(ANA);
        Integer livroRecusado = idsDosLivros(ANA).getFirst();

        avaliar(ANA, itens.getFirst(), "POSITIVA");
        avaliar(ANA, itens.getFirst(), "NEGATIVA");

        assertThat(idsDosItens(ANA)).doesNotContain(itens.getFirst()).hasSize(itens.size() - 1);
        assertThat(chamadas()).isEqualTo(1);

        AGORA.set(AGORA.get().plusDays(2));
        assertThat(idsDosLivros(ANA)).isNotEmpty().doesNotContain(livroRecusado);
    }

    @Test
    void rnfRec001_avaliacaoPositivaFicaNoItem() throws Exception {
        Integer item = idsDosItens(ANA).getFirst();

        avaliar(ANA, item, "POSITIVA");

        mvc.perform(como(ANA, get("/api/me/recomendacoes")))
                .andExpect(jsonPath("$[0].id").value(item))
                .andExpect(jsonPath("$[0].avaliacao").value("POSITIVA"));
    }

    @Test
    void rnfRec001_itemDeOutroClienteRespondeNaoEncontrado() throws Exception {
        Integer itemDaAna = idsDosItens(ANA).getFirst();

        mvc.perform(como(BRUNO, put("/api/me/recomendacoes/itens/{id}/avaliacao", itemDaAna))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avaliacao\":\"NEGATIVA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rnfRec001_avaliacaoAusenteRespondeRequisicaoInvalida() throws Exception {
        Integer item = idsDosItens(ANA).getFirst();

        mvc.perform(como(ANA, put("/api/me/recomendacoes/itens/{id}/avaliacao", item))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rnfRec001_administradorNaoTemRecomendacao() throws Exception {
        mvc.perform(como(ADMIN, get("/api/me/recomendacoes"))).andExpect(status().isForbidden());
    }
}
