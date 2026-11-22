package br.fatec.les.livraria.clientes.infraestrutura.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

/**
 * Consulta de clientes do administrador sobre o seed 201_clientes.sql (RF-CLI-004, DEC-07, DEC-10). Seed: Ana
 * (FEMININO, 1990, SP), Bruno (MASCULINO, 1985, RJ), Carla (FEMININO, 1992, MG, INATIVA), Diego (MASCULINO, 1978, PR,
 * ENCERRADA), Elisa (NAO_INFORMADO, 1995, RS e SP) e mais cinco clientes (Fernanda, Gustavo, Helena, Ivan e Juliana),
 * todos NAO_INFORMADO, nascidos antes de 1990 e em UFs novas, para não alterar os filtros existentes.
 */
@SpringBootTest
class ClienteAdminControllerTest {

    private static final String ROTA = "/api/admin/clientes";

    /** 2026-10-24T12:00 em America/Sao_Paulo, o instante citado no seed: Carla INATIVA e Diego ENCERRADO. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @TestConfiguration
    static class RelogioFixo {

        @Bean
        @Primary
        Relogio relogioFixo() {
            return () -> AGORA;
        }
    }

    @Autowired private WebApplicationContext contexto;

    @Autowired private UsuarioRepositorio usuarios;

    @Autowired private EmissorDeToken emissor;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void rfCli004_semFiltroListaTodosEmOrdemDeNomeNoEnvelope() throws Exception {
        mvc.perform(comoAdmin(get(ROTA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(10))
                .andExpect(
                        jsonPath(
                                "$.itens[*].nome",
                                contains(
                                        "Ana Souza",
                                        "Bruno Lima",
                                        "Carla Mendes",
                                        "Diego Rocha",
                                        "Elisa Prado",
                                        "Fernanda Alves",
                                        "Gustavo Nunes",
                                        "Helena Castro",
                                        "Ivan Barbosa",
                                        "Juliana Dias")))
                .andExpect(jsonPath("$.itens[0].id").value(1))
                .andExpect(jsonPath("$.itens[0].email").value("ana.souza@livraria.test"))
                .andExpect(jsonPath("$.itens[0].situacao").value("ATIVA"));
    }

    @Test
    void rfCli004_paginaEOrdenaPorDataDeNascimento() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("tamanho", "2").param("ordem", "dataNascimento,desc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPaginas").value(5))
                .andExpect(jsonPath("$.itens[*].nome", contains("Elisa Prado", "Carla Mendes")));
    }

    @Test
    void rfCli004_nomeSemDiferenciarMaiusculas() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("nome", "LIMA")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Bruno Lima")));
    }

    @Test
    void rfCli004_cidadeSemAcentoNaoDuplicaClienteComDoisEnderecos() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("cidade", "sao paulo")))
                .andExpect(jsonPath("$.totalItens").value(2))
                .andExpect(jsonPath("$.itens[*].nome", contains("Ana Souza", "Elisa Prado")));
    }

    @Test
    void rfCli004_cidadeEUfValemParaOMesmoEndereco() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("cidade", "porto alegre").param("uf", "SP")))
                .andExpect(jsonPath("$.totalItens").value(0));
        mvc.perform(comoAdmin(get(ROTA).param("cidade", "porto alegre").param("uf", "RS")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Elisa Prado")));
    }

    @Test
    void rfCli004_cpfETelefoneComMascara() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("cpf", "529.982.247-25")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Ana Souza")));
        mvc.perform(comoAdmin(get(ROTA).param("telefone", "(21) 98888-7002")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Bruno Lima")));
    }

    @Test
    void rfCli004_emailContem() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("email", "MENDES@")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Carla Mendes")));
    }

    @Test
    void rfCli004_situacaoVemDoAcesso() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("situacao", "INATIVA")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Carla Mendes")))
                .andExpect(jsonPath("$.itens[0].situacao").value("INATIVA"));
        mvc.perform(comoAdmin(get(ROTA).param("situacao", "ENCERRADA")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Diego Rocha")));
        mvc.perform(comoAdmin(get(ROTA).param("situacao", "INATIVA").param("situacao", "ENCERRADA")))
                .andExpect(jsonPath("$.totalItens").value(2));
    }

    @Test
    void rfCli004_valoresRepetidosSeCombinamComOu() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("genero", "FEMININO").param("genero", "MASCULINO")))
                .andExpect(
                        jsonPath(
                                "$.itens[*].nome",
                                containsInAnyOrder("Ana Souza", "Bruno Lima", "Carla Mendes", "Diego Rocha")));
    }

    @Test
    void rfCli004_camposDiferentesSeCombinamComE() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("genero", "FEMININO").param("uf", "SP")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Ana Souza")));
        mvc.perform(
                        comoAdmin(
                                get(ROTA)
                                        .param("dataNascimentoDe", "1990-01-01")
                                        .param("dataNascimentoAte", "1995-12-31")))
                .andExpect(jsonPath("$.itens[*].nome", contains("Ana Souza", "Carla Mendes", "Elisa Prado")));
    }

    @Test
    void rfCli004_filtroResolvidoNoAcessoSemResultadoDevolvePaginaVazia() throws Exception {
        mvc.perform(comoAdmin(get(ROTA).param("email", "ninguem@nada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(0))
                .andExpect(jsonPath("$.itens", hasSize(0)));
    }

    @Test
    void rfCli004_parametrosInvalidosRespondem400() throws Exception {
        mvc.perform(
                        comoAdmin(
                                get(ROTA)
                                        .param("dataNascimentoDe", "1995-01-01")
                                        .param("dataNascimentoAte", "1990-01-01")))
                .andExpect(status().isBadRequest());
        mvc.perform(comoAdmin(get(ROTA).param("ordem", "cpf,asc"))).andExpect(status().isBadRequest());
        mvc.perform(comoAdmin(get(ROTA).param("telefone", "abc"))).andExpect(status().isBadRequest());
    }

    @Test
    void rfCli004_detalheTrazEnderecosECartoesMascarados() throws Exception {
        mvc.perform(comoAdmin(get(ROTA + "/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.enderecos", hasSize(2)))
                .andExpect(jsonPath("$.cartoes", hasSize(1)))
                .andExpect(jsonPath("$.cartoes[0].numero", not("4111111111111111")))
                .andExpect(jsonPath("$.cartoes[0].bandeira.nome").value("Visa"));
    }

    @Test
    void rfCli004_detalheDeClienteInexistenteResponde404() throws Exception {
        mvc.perform(comoAdmin(get(ROTA + "/999999"))).andExpect(status().isNotFound());
    }

    @Test
    void dec08_consultaExigePapelAdmin() throws Exception {
        mvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mvc.perform(get(ROTA).cookie(cookieDoUsuario(2L))).andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder requisicao) {
        return requisicao.cookie(cookieDoUsuario(1L));
    }

    private Cookie cookieDoUsuario(Long id) {
        String token = emissor.emitir(usuarios.buscarPorId(id).orElseThrow()).valor();
        return new Cookie(EmissorDeToken.COOKIE, token);
    }
}
