package br.fatec.les.livraria.compartilhado.infraestrutura.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.fatec.les.livraria.compartilhado.aplicacao.TarefaPorTempo;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:e2e;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "livraria.seed.local=classpath:seed-e2e/*.sql"
        })
@ActiveProfiles({"h2", "e2e"})
class ApoioAosTestesControllerTest {

    static class TarefaContada implements TarefaPorTempo {

        final AtomicInteger execucoes = new AtomicInteger();

        @Override
        public void executar() {
            execucoes.incrementAndGet();
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        TarefaContada tarefaContada() {
            return new TarefaContada();
        }
    }

    @Autowired private WebApplicationContext contexto;

    @Autowired private DataSource dataSource;

    @Autowired private Relogio relogio;

    @Autowired private TarefaContada tarefa;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void resetApagaOsDadosEReexecutaOSeed() throws Exception {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        jdbc.sql("insert into exemplo (id, nome) values (2, 'do teste')").update();

        mvc.perform(post("/api/teste/reset")).andExpect(status().isNoContent());

        assertThat(jdbc.sql("select nome from exemplo").query(String.class).list()).containsExactly("do seed");
    }

    @Test
    void relogioFixadoValeNoFusoDaLojaEExecutaAsTarefas() throws Exception {
        int antes = tarefa.execucoes.get();

        mvc.perform(
                        post("/api/teste/relogio")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"agora\":\"2026-10-21T14:30:00Z\"}"))
                .andExpect(status().isNoContent());

        assertThat(relogio.agora()).isEqualTo(LocalDateTime.of(2026, 10, 21, 11, 30));
        assertThat(tarefa.execucoes.get()).isEqualTo(antes + 1);

        mvc.perform(post("/api/teste/reset")).andExpect(status().isNoContent());
        assertThat(relogio.agora()).isNotEqualTo(LocalDateTime.of(2026, 10, 21, 11, 30));
    }
}
