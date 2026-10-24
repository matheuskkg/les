package br.fatec.les.livraria.clientes.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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

import com.jayway.jsonpath.JsonPath;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import jakarta.servlet.http.Cookie;

/**
 * Endereços (RF-CLI-006, RNF-CLI-004) e cartões (RF-CLI-007, RN-CLI-004, RN-CLI-005, DEC-06, DEC-10) do cliente
 * logado, sobre o seed 201_clientes.sql: Ana (usuário 2) tem os endereços 1 e 2 e o cartão 1; Bruno (usuário 3) tem
 * só o endereço 3 e o cartão 2; Elisa (usuário 6) tem o cartão 4 (preferencial) e o 5.
 */
@SpringBootTest
@Transactional
class EnderecosECartoesIntegracaoTest {

    private static final long ANA = 2L;
    private static final long BRUNO = 3L;
    private static final long ELISA = 6L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @TestConfiguration
    static class RelogioFixo {

        @Bean
        @Primary
        Relogio relogioFixo() {
            return () -> AGORA;
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
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfCli006_listaAdicionaAlteraERemoveEnderecos() throws Exception {
        mvc.perform(get("/api/me/enderecos").cookie(sessaoDe(ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        escrever(post("/api/me/enderecos"), ANA, endereco("Apartamento", "77"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCurto").value("Apartamento"))
                .andExpect(jsonPath("$.pais").value("Brasil"));

        escrever(put("/api/me/enderecos/1"), ANA, endereco("Casa", "124"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("124"));

        escrever(delete("/api/me/enderecos/2"), ANA, "").andExpect(status().isNoContent());
        mvc.perform(get("/api/me/enderecos").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", not(hasItem(2))));
    }

    @Test
    void rnCli003_enderecoIncompletoResponde422() throws Exception {
        String semCep = endereco("Casa nova", "10").replace("\"01415000\"", "\"123\"");
        escrever(post("/api/me/enderecos"), ANA, semCep)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-CLI-003")));
    }

    @Test
    void rnCli001_naoRemoveOUnicoEndereco() throws Exception {
        escrever(delete("/api/me/enderecos/3"), BRUNO, "")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-CLI-001")));
    }

    @Test
    void dec07_enderecoECartaoDeOutroClienteRespondem404() throws Exception {
        escrever(delete("/api/me/enderecos/3"), ANA, "").andExpect(status().isNotFound());
        escrever(put("/api/me/enderecos/3"), ANA, endereco("Casa", "1")).andExpect(status().isNotFound());
        escrever(delete("/api/me/cartoes/2"), ANA, "").andExpect(status().isNotFound());
    }

    @Test
    void rfCli007_listaCartoesComNumeroMascarado() throws Exception {
        mvc.perform(get("/api/me/cartoes").cookie(sessaoDe(ANA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroMascarado", endsWith("1111")))
                .andExpect(jsonPath("$[0].numeroMascarado", not("4111111111111111")))
                .andExpect(jsonPath("$[0].bandeira.nome").value("Visa"))
                .andExpect(jsonPath("$[0].preferencial").value(true));
    }

    @Test
    void rfCli007_novoCartaoEntraNaoPreferencialEPodeVirarOPreferencial() throws Exception {
        String resposta = escrever(post("/api/me/cartoes"), ANA, cartao(2, "2030-01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.preferencial").value(false))
                .andReturn().getResponse().getContentAsString();
        Integer novo = JsonPath.read(resposta, "$.id");

        escrever(post("/api/me/cartoes/" + novo + "/preferencial"), ANA, "").andExpect(status().isNoContent());

        mvc.perform(get("/api/me/cartoes").cookie(sessaoDe(ANA)))
                .andExpect(jsonPath("$[?(@.preferencial == true)].id", hasSize(1)))
                .andExpect(jsonPath("$[?(@.preferencial == true)].id", hasItem(novo)));
    }

    @Test
    void dec06_cartaoVencidoNoMesCorrenteResponde422() throws Exception {
        escrever(post("/api/me/cartoes"), ANA, cartao(2, "2026-09"))
                .andExpect(status().isUnprocessableContent());
        escrever(post("/api/me/cartoes"), ANA, cartao(2, "2026-10"))
                .andExpect(status().isCreated());
    }

    @Test
    void rnCli005_bandeiraNaoRegistradaResponde422() throws Exception {
        escrever(post("/api/me/cartoes"), ANA, cartao(99, "2030-01"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItem("RN-CLI-005")));
    }

    @Test
    void rfCli007_excluirOPreferencialPromoveOMaisRecente() throws Exception {
        escrever(delete("/api/me/cartoes/4"), ELISA, "").andExpect(status().isNoContent());

        mvc.perform(get("/api/me/cartoes").cookie(sessaoDe(ELISA)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].preferencial").value(true));
    }

    @Test
    void dec08_rotasExigemSessaoDeCliente() throws Exception {
        mvc.perform(get("/api/me/enderecos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me/cartoes").cookie(sessaoDe(1L))).andExpect(status().isForbidden());
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

    private static String endereco(String nomeCurto, String numero) {
        return """
                {
                  "nomeCurto": "%s",
                  "tipoEndereco": "CASA",
                  "tipoLogradouro": "RUA",
                  "logradouro": "Rua das Flores",
                  "numero": "%s",
                  "bairro": "Jardim Paulista",
                  "cep": "01415000",
                  "cidade": "São Paulo",
                  "uf": "SP"
                }
                """.formatted(nomeCurto, numero);
    }

    private static String cartao(int bandeiraId, String validade) {
        return """
                { "numero": "5555555555554444", "nomeImpresso": "ANA SOUZA", "bandeiraId": %d, "validade": "%s" }
                """.formatted(bandeiraId, validade);
    }
}
