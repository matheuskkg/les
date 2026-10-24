package br.fatec.les.livraria.compartilhado.infraestrutura.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
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
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 3, 4, 9, 15, 30);

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

        /** Relógio fixo: a revisão precisa gravar exatamente o instante da porta {@link Relogio} (DEC-08). */
        @Bean
        @Primary
        Relogio relogioFixo() {
            return () -> AGORA;
        }

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
        // agendada de verdade roda sem ela. Limpar aqui reproduz o contexto de ExpirarReservas e do seed, e o
        // finally devolve o contexto que o Spring limpou ao fim da requisição.
        RequestAttributes anteriores = RequestContextHolder.getRequestAttributes();
        RequestContextHolder.resetRequestAttributes();
        try {
            EscritaDeEstoque escrita = new TransactionTemplate(transacoes).execute(status -> {
                EstoqueJpa estoque = estoques.findByLivroId(LIVRO_COM_ESTOQUE).orElseThrow();
                estoque.setQuantidade(estoque.getQuantidade() + 1);
                estoques.save(estoque);
                return new EscritaDeEstoque(estoque.getId(), estoque.getQuantidade());
            });

            Revisao revisao = revisaoMaisRecenteDeEstoque(escrita.estoqueId());
            assertThat(revisao.usuario()).isEqualTo(UsuarioAtual.SISTEMA);
            assertThat(revisao.usuarioId()).isNull();
            assertThat(revisao.dataHora())
                    .as("a revisão usa o instante da porta Relogio, não o fuso padrão da JVM")
                    .isEqualTo(AGORA);

            var linha = jdbc.sql(
                    "select quantidade, tipo_revisao from estoque_aud where revisao = ? and id = ?")
                    .param(revisao.id())
                    .param(escrita.estoqueId())
                    .query()
                    .singleRow();
            assertThat(((Number) linha.get("quantidade")).intValue()).isEqualTo(escrita.quantidade());
            assertThat(((Number) linha.get("tipo_revisao")).intValue())
                    .isEqualTo(RevisionType.MOD.getRepresentation().intValue());
        } finally {
            RequestContextHolder.setRequestAttributes(anteriores);
        }
    }

    @Test
    void loginQueReativaContaRegistraRevisaoAnonima() throws Exception {
        Usuario reativada = usuarios.salvar(new Usuario(null, EMAIL_REATIVADA, null, senhas.codificar(SENHA),
                Papel.CLIENTE, relogio.agora().minusDays(1)));
        Cookie csrf = tokenCsrf();

        mvc.perform(post("/api/sessao")
                .cookie(csrf)
                .header(CABECALHO_CSRF, csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(EMAIL_REATIVADA, SENHA)))
                .andExpect(status().isOk());

        Revisao revisao = revisaoMaisRecenteDeUsuario(reativada.getId());
        assertThat(revisao.usuario()).isEqualTo(UsuarioAtual.ANONIMO);
        assertThat(revisao.usuarioId()).isNull();
    }

    @Test
    void escritaAutenticadaRegistraOUsuarioDoAdmin() throws Exception {
        Cookie csrf = tokenCsrf();
        Cookie sessao = new Cookie(EmissorDeToken.COOKIE, emissor.emitir(administrador()).valor());
        Long estoqueId = estoques.findByLivroId(LIVRO_COM_ESTOQUE).orElseThrow().getId();

        mvc.perform(put("/api/admin/auditoria/estoque")
                .cookie(sessao, csrf)
                .header(CABECALHO_CSRF, csrf.getValue()))
                .andExpect(status().isOk());

        Revisao revisao = revisaoMaisRecenteDeEstoque(estoqueId);
        assertThat(revisao.usuario()).isEqualTo(ADMIN);
        assertThat(revisao.usuarioId()).isEqualTo(1L);
    }

    @Test
    void tabelaDeAuditoriaDoUsuarioNaoGuardaOSenhaHash() {
        Long colunas = jdbc.sql("""
                select count(*)
                from information_schema.columns
                where lower(table_name) = 'usuario_aud'
                  and lower(column_name) = 'senha_hash'
                """)
                .query(Long.class)
                .single();

        assertThat(colunas).as("a senha fica fora de usuario_aud (RNF-CLI-003)").isZero();
    }

    private Usuario administrador() {
        return usuarios.buscarPorId(1L).orElseThrow();
    }

    /** Revisão gravada para a linha de estoque escrita, pelo vínculo de {@code estoque_aud} com {@code revisao}. */
    private Revisao revisaoMaisRecenteDeEstoque(Long estoqueId) {
        List<Revisao> revisoes = jdbc.sql("""
                select r.id, r.usuario_id, r.usuario, r.data_hora
                from revisao r
                join estoque_aud e on e.revisao = r.id
                where e.id = ?
                order by r.id desc
                """)
                .param(estoqueId)
                .query(AuditoriaIntegracaoTest::mapearRevisao)
                .list();

        assertThat(revisoes).as("revisão da escrita do estoque %s", estoqueId).isNotEmpty();
        return revisoes.get(0);
    }

    /** Revisão da escrita da conta, pelo vínculo de {@code usuario_aud} com {@code revisao}. */
    private Revisao revisaoMaisRecenteDeUsuario(Long usuarioId) {
        List<Revisao> revisoes = jdbc.sql("""
                select r.id, r.usuario_id, r.usuario, r.data_hora
                from revisao r
                join usuario_aud u on u.revisao = r.id
                where u.id = ?
                order by r.id desc
                """)
                .param(usuarioId)
                .query(AuditoriaIntegracaoTest::mapearRevisao)
                .list();

        assertThat(revisoes).as("revisão da escrita do usuário %s", usuarioId).isNotEmpty();
        return revisoes.get(0);
    }

    private static Revisao mapearRevisao(ResultSet rs, int numero) throws SQLException {
        return new Revisao(
                rs.getLong("id"),
                rs.getObject("usuario_id", Long.class),
                rs.getString("usuario"),
                rs.getObject("data_hora", LocalDateTime.class));
    }

    /** GET /api/sessao emite o cookie CSRF mesmo sem sessão (DEC-08); ele vale para a próxima escrita. */
    private Cookie tokenCsrf() throws Exception {
        Cookie csrf = mvc.perform(get("/api/sessao")).andReturn().getResponse().getCookie(COOKIE_CSRF);
        assertThat(csrf).isNotNull();
        return csrf;
    }

    private record Revisao(Long id, Long usuarioId, String usuario, LocalDateTime dataHora) {
    }

    private record EscritaDeEstoque(Long estoqueId, Integer quantidade) {
    }
}
