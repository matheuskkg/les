package br.fatec.les.livraria.clientes.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import jakarta.servlet.http.Cookie;

/**
 * Autocadastro de cliente de ponta a ponta (RF-CLI-001, RN-CLI-001 a RN-CLI-006, RNF-CLI-001, RNF-CLI-002,
 * RNF-CLI-005; DEC-07, DEC-10): rota pública sobre o seed em H2, com CSRF de SPA.
 */
@SpringBootTest
@Transactional
class CadastroDeClienteIntegracaoTest {

    private static final String COOKIE_CSRF = "XSRF-TOKEN";
    private static final String CABECALHO_CSRF = "X-XSRF-TOKEN";
    private static final String SENHA = "Senha@123";
    /** 2026-10-24T12:00 em America/Sao_Paulo, o instante citado no comentário do seed 201_clientes.sql. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    /** Relógio fixo: Carla e Diego do seed têm situações de conta que dependem do instante da consulta (DEC-10). */
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

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfCli001_rnfCli005_cadastroValidoResponde201EOLoginDoClienteFunciona() throws Exception {
        MockHttpServletResponse resposta = cadastrar(corpoDeNovoCliente()).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/me"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(10000)))
                .andReturn().getResponse();

        Long clienteId = usuarios.buscarPorLogin("novo.cliente@livraria.test").orElseThrow().getId();
        assertThat(clienteId).isGreaterThanOrEqualTo(10000L);

        // RNF-CLI-005: o cliente recém-cadastrado consegue entrar com o e-mail e a senha informados.
        entrar("novo.cliente@livraria.test", SENHA).andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CLIENTE"));

        assertThat(resposta.getContentAsString()).contains("\"id\"");
    }

    @Test
    void rfCli001_cadastroComVariasViolacoesResponde422ComAList() throws Exception {
        mvc.perform(comTokenCsrf(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content(corpoInvalido()))).andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("A operação viola regras de negócio."))
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItems("RN-CLI-001", "RN-CLI-005",
                        "RN-CLI-006", "RNF-CLI-001", "RNF-CLI-002")));
    }

    @Test
    void rfCli001_cadastroComDadosDeContaEmUsoResponde422() throws Exception {
        // Carla (usuário 4) foi inativada há 4 dias: ainda está em uso, então CPF, e-mail e telefone bloqueiam (DEC-10).
        mvc.perform(comTokenCsrf(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content(corpoDeCarla()))).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violacoes[*].codigo", hasItems("RF-CLI-001")))
                .andExpect(jsonPath("$.violacoes.length()").value(3));
    }

    @Test
    void rfCli001_dadosDeContaEncerradaLiberamCpfEmailETelefone() throws Exception {
        // Diego (usuário 5) foi inativado há mais de 7 dias: a conta está encerrada e libera os dados (DEC-10).
        cadastrar(corpoDeDiego()).andExpect(status().isCreated());
    }

    @Test
    void rfCli001_enderecoNuloNaListaResponde422EmVezDeErro500() throws Exception {
        cadastrar(corpoDeNovoCliente("[null]", cartaoJson()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violacoes.length()").value(1))
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-CLI-003"))
                .andExpect(jsonPath("$.violacoes[0].mensagem")
                        .value("Endereço 1: informe os dados do endereço."));
    }

    @Test
    void rfCli001_cartaoNuloNaListaResponde422EmVezDeErro500() throws Exception {
        cadastrar(corpoDeNovoCliente(enderecoJson(), "[null]"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violacoes.length()").value(1))
                .andExpect(jsonPath("$.violacoes[0].codigo").value("RN-CLI-004"))
                .andExpect(jsonPath("$.violacoes[0].mensagem").value("Cartão 1: informe os dados do cartão."));
    }

    @Test
    void rfCli001_nomeAcimaDoTamanhoDaColunaResponde422() throws Exception {
        cadastrar(corpoComDados("a".repeat(121), "1991-05-20", "11122233396",
                "nome.longo@livraria.test", "988880001", "11"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violacoes[*].mensagem",
                        hasItem("O nome do cliente deve ter no máximo 120 caracteres.")));
    }

    @Test
    void rfCli006_dataDeNascimentoNoFuturoResponde422() throws Exception {
        cadastrar(corpoComDados("Novo Cliente", "2030-01-01", "11122233396",
                "futuro@livraria.test", "988880001", "11"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violacoes[*].mensagem",
                        hasItem("A data de nascimento não pode estar no futuro.")));
    }

    private ResultActions cadastrar(String corpo) throws Exception {
        return mvc.perform(comTokenCsrf(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo)));
    }

    private ResultActions entrar(String login, String senha) throws Exception {
        return mvc.perform(comTokenCsrf(post("/api/sessao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, senha))));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder comTokenCsrf(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao) throws Exception {
        Cookie csrf = tokenCsrf();
        return requisicao.cookie(csrf).header(CABECALHO_CSRF, csrf.getValue());
    }

    /** GET /api/sessao emite o cookie CSRF mesmo sem sessão (DEC-08); ele vale para a próxima escrita. */
    private Cookie tokenCsrf() throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie(COOKIE_CSRF);
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private static String corpoDeNovoCliente() {
        return corpoDeNovoCliente(enderecoJson(), cartaoJson());
    }

    private static String corpoDeNovoCliente(String enderecos, String cartoes) {
        return """
                {
                  "nome": "Novo Cliente",
                  "genero": "MASCULINO",
                  "dataNascimento": "1991-05-20",
                  "cpf": "11122233396",
                  "email": "novo.cliente@livraria.test",
                  "telefone": { "tipo": "CELULAR", "ddd": "11", "numero": "988880001" },
                  "senha": "%1$s",
                  "confirmacaoSenha": "%1$s",
                  "enderecos": %2$s,
                  "cartoes": %3$s
                }
                """.formatted(SENHA, enderecos, cartoes);
    }

    private static String enderecoJson() {
        return """
                [
                  { "nomeCurto": "Casa", "tipoEndereco": "CASA", "tipoLogradouro": "RUA",
                    "logradouro": "Rua Nova", "numero": "10", "bairro": "Centro",
                    "cep": "01001000", "cidade": "São Paulo", "uf": "SP", "observacoes": null }
                ]
                """;
    }

    private static String cartaoJson() {
        return """
                [
                  { "numero": "4111111111111111", "nomeImpresso": "NOVO CLIENTE",
                    "bandeiraId": 1, "validade": "2030-12" }
                ]
                """;
    }

    private static String corpoInvalido() {
        return """
                {
                  "nome": "",
                  "genero": "FEMININO",
                  "dataNascimento": "1990-01-01",
                  "cpf": "12345678900",
                  "email": "sem-arroba",
                  "telefone": { "tipo": "CELULAR", "ddd": "1", "numero": "123" },
                  "senha": "senha",
                  "confirmacaoSenha": "outra",
                  "enderecos": [],
                  "cartoes": [
                    { "numero": "123", "nomeImpresso": "", "bandeiraId": 9999, "validade": "2020-01" }
                  ]
                }
                """;
    }

    private static String corpoDeCarla() {
        return corpoComDados("Carla Mendes", "1992-11-02", "12345678909", "carla.mendes@livraria.test",
                "988887003", "31");
    }

    private static String corpoDeDiego() {
        return corpoComDados("Diego Rocha", "1978-01-30", "98765432100", "diego.rocha@livraria.test",
                "988887004", "41");
    }

    private static String corpoComDados(String nome, String nascimento, String cpf, String email, String numero,
            String ddd) {
        return """
                {
                  "nome": "%1$s",
                  "genero": "MASCULINO",
                  "dataNascimento": "%2$s",
                  "cpf": "%3$s",
                  "email": "%4$s",
                  "telefone": { "tipo": "CELULAR", "ddd": "%6$s", "numero": "%5$s" },
                  "senha": "%7$s",
                  "confirmacaoSenha": "%7$s",
                  "enderecos": [
                    { "nomeCurto": "Casa", "tipoEndereco": "CASA", "tipoLogradouro": "RUA",
                      "logradouro": "Rua Teste", "numero": "500", "bairro": "Centro",
                      "cep": "01001000", "cidade": "São Paulo", "uf": "SP", "observacoes": null }
                  ],
                  "cartoes": [
                    { "numero": "5555555555554444", "nomeImpresso": "%1$s",
                      "bandeiraId": 2, "validade": "2030-12" }
                  ]
                }
                """.formatted(nome, nascimento, cpf, email, numero, ddd, SENHA);
    }
}
