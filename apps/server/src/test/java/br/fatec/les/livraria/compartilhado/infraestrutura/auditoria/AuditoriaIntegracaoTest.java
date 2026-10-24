package br.fatec.les.livraria.compartilhado.infraestrutura.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import br.fatec.les.livraria.estoque.infraestrutura.persistencia.EstoqueJpa;
import br.fatec.les.livraria.estoque.infraestrutura.persistencia.EstoqueJpaRepository;
import jakarta.servlet.http.Cookie;

/**
 * Log de transação com o Hibernate Envers e o profile H2 (RNF-GER-002, DEC-08, DEC-14). Verifica o autor gravado
 * nas três situações: escrita fora de requisição HTTP ({@code sistema}), escrita sem login ({@code anonimo}) e
 * escrita autenticada (e-mail e id do usuário).
 */
@SpringBootTest
class AuditoriaIntegracaoTest {

    private static final String ADMIN = "admin@livraria.test";
    private static final String EMAIL_REATIVADA = "auditoria.reativada@livraria.test";
    private static final String SENHA = "Senha@123";
    private static final String COOKIE_CSRF = "XSRF-TOKEN";
    private static final String CABECALHO_CSRF = "X-XSRF-TOKEN";
    private static final long LIVRO_COM_ESTOQUE = 1L;

    /** Escrita autenticada de teste: não há rota de escrita de estoque no MVP (RF-EST-001 está fora). */
    @RestController
    static class ControllerDeEscritaAuditada {

        private final EstoqueJpaRepository estoques;

        ControllerDeEscritaAuditada(EstoqueJpaRepository estoques) {
            this.estoques = estoques;
        }

        @PutMapping("/api/admin/auditoria/estoque")
        void alterarEstoque() {
            EstoqueJpa estoque = estoques.findByLivroId(LIVRO_COM_ESTOQUE).orElseThrow();
            estoque.setQuantidade(estoque.getQuantidade() + 1);
            estoques.save(estoque);
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDeEscritaAuditada controllerDeEscritaAuditada(EstoqueJpaRepository estoques) {
            return new ControllerDeEscritaAuditada(estoques);
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private CodificadorDeSenha senhas;

    @Autowired
    private Relogio relogio;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private EstoqueJpaRepository estoques;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transacoes;

    private MockMvc mvc;
    private JdbcClient jdbc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        jdbc = JdbcClient.create(dataSource);
    }

    @Test
    void escritaForaDeRequisicaoHttpRegistraRevisaoDoSistema() {
        // O TestContext do @SpringBootTest (MOCK) deixa uma requisição simulada presa na thread; uma tarefa
        // agendada de verdade roda sem ela. Limpar aqui reproduz o contexto de ExpirarReservas e do seed.
        RequestContextHolder.resetRequestAttributes();
        long anterior = ultimaRevisao();

        Integer novaQuantidade = new TransactionTemplate(transacoes).execute(status -> {
            EstoqueJpa estoque = estoques.findByLivroId(LIVRO_COM_ESTOQUE).orElseThrow();
            estoque.setQuantidade(estoque.getQuantidade() + 1);
            estoques.save(estoque);
            return estoque.getQuantidade();
        });

        Revisao revisao = unicaRevisaoDepoisDe(anterior);
        assertThat(revisao.usuario()).isEqualTo(UsuarioAtual.SISTEMA);
        assertThat(revisao.usuarioId()).isNull();
        assertThat(revisao.dataHora()).isNotNull();

        Map<String, Object> linha = jdbc.sql(
                "select quantidade, tipo_revisao from estoque_aud where revisao = ?")
                .param(revisao.id())
                .query()
                .singleRow();
        assertThat(((Number) linha.get("quantidade")).intValue()).isEqualTo(novaQuantidade);
        assertThat(((Number) linha.get("tipo_revisao")).intValue())
                .isEqualTo(RevisionType.MOD.getRepresentation().intValue());
    }

    @Test
    void loginQueReativaContaRegistraRevisaoAnonima() throws Exception {
        usuarios.salvar(new Usuario(null, EMAIL_REATIVADA, null, senhas.codificar(SENHA), Papel.CLIENTE,
                relogio.agora().minusDays(1)));
        long anterior = ultimaRevisao();
        Cookie csrf = tokenCsrf();

        mvc.perform(post("/api/sessao")
                .cookie(csrf)
                .header(CABECALHO_CSRF, csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(EMAIL_REATIVADA, SENHA)))
                .andExpect(status().isOk());

        Revisao revisao = unicaRevisaoDepoisDe(anterior);
        assertThat(revisao.usuario()).isEqualTo(UsuarioAtual.ANONIMO);
        assertThat(revisao.usuarioId()).isNull();
    }

    @Test
    void escritaAutenticadaRegistraOUsuarioDoAdmin() throws Exception {
        Cookie csrf = tokenCsrf();
        Cookie sessao = new Cookie(EmissorDeToken.COOKIE, emissor.emitir(administrador()).valor());
        long anterior = ultimaRevisao();

        mvc.perform(put("/api/admin/auditoria/estoque")
                .cookie(sessao, csrf)
                .header(CABECALHO_CSRF, csrf.getValue()))
                .andExpect(status().isOk());

        Revisao revisao = unicaRevisaoDepoisDe(anterior);
        assertThat(revisao.usuario()).isEqualTo(ADMIN);
        assertThat(revisao.usuarioId()).isEqualTo(1L);
    }

    private Usuario administrador() {
        return usuarios.buscarPorId(1L).orElseThrow();
    }

    private long ultimaRevisao() {
        return jdbc.sql("select coalesce(max(id), 0) from revisao").query(Long.class).single();
    }

    private Revisao unicaRevisaoDepoisDe(long anterior) {
        List<Revisao> revisoes = jdbc.sql(
                "select id, usuario_id, usuario, data_hora from revisao where id > ? order by id")
                .param(anterior)
                .query((rs, numero) -> new Revisao(
                        rs.getLong("id"),
                        rs.getObject("usuario_id", Long.class),
                        rs.getString("usuario"),
                        rs.getObject("data_hora", LocalDateTime.class)))
                .list();
        assertThat(revisoes).hasSize(1);
        return revisoes.get(0);
    }

    /** GET /api/sessao emite o cookie CSRF mesmo sem sessão (DEC-08); ele vale para a próxima escrita. */
    private Cookie tokenCsrf() throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie(COOKIE_CSRF);
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private record Revisao(Long id, Long usuarioId, String usuario, LocalDateTime dataHora) {
    }
}
