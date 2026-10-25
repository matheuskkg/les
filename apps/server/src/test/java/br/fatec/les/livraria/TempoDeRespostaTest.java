package br.fatec.les.livraria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import jakarta.servlet.http.Cookie;

/**
 * Consultas de usuário respondem em até 1 segundo com o seed completo, inclusive os pedidos históricos do ANA-01
 * (RNF-GER-001, DEC-07, DEC-14). Cada rota roda uma vez para aquecer e vale a melhor de três medições, para que uma
 * pausa do GC ou um vizinho barulhento na máquina da CI não reprove a consulta. Sem {@code @Transactional}: cada
 * requisição abre a própria sessão do Hibernate, como no uso real, e só lê.
 */
@SpringBootTest
class TempoDeRespostaTest {

    private static final Duration LIMITE = Duration.ofSeconds(1);

    private static final int MEDICOES = 3;

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

    // Usuário 1 é o admin; o 6 é a Elisa, com os pedidos históricos.
    @ParameterizedTest(name = "{1}")
    @CsvSource(delimiter = '|', value = {
            "1 | /api/livros?tamanho=100",
            "1 | /api/livros?titulo=a&tamanho=100",
            "1 | /api/livros?q=a&tamanho=100",
            "1 | /api/admin/clientes?tamanho=100",
            "1 | /api/admin/clientes?nome=a&situacao=ATIVA&situacao=INATIVA",
            "1 | /api/admin/clientes/5",
            "1 | /api/admin/pedidos?tamanho=100",
            "1 | /api/admin/pedidos?status=ENTREGUE&tamanho=100&pagina=2",
            "1 | /api/admin/pedidos/1",
            "1 | /api/admin/pedidos/contagem",
            "1 | /api/admin/trocas?tamanho=100",
            "1 | /api/admin/trocas?status=TROCADO&tamanho=100",
            "1 | /api/admin/analise?inicio=2024-11&fim=2026-10&categoria=1&categoria=2&categoria=3&categoria=4&categoria=5",
            "1 | /api/admin/analise/exportacao?inicio=2024-11&fim=2026-10&categoria=1&categoria=2&categoria=3&categoria=4&categoria=5",
            "6 | /api/me",
            "6 | /api/me/pedidos?tamanho=100",
            "6 | /api/me/notificacoes?tamanho=100" })
    void rnfGer001_consultaRespondeEmAteUmSegundo(Long usuarioId, String rota) throws Exception {
        Cookie sessao = new Cookie(EmissorDeToken.COOKIE,
                emissor.emitir(usuarios.buscarPorId(usuarioId).orElseThrow()).valor());
        mvc.perform(get(rota).cookie(sessao)).andExpect(status().isOk());

        Duration melhor = null;
        for (int i = 0; i < MEDICOES; i++) {
            long inicio = System.nanoTime();
            mvc.perform(get(rota).cookie(sessao)).andExpect(status().isOk());
            Duration tempo = Duration.ofNanos(System.nanoTime() - inicio);
            melhor = melhor == null || tempo.compareTo(melhor) < 0 ? tempo : melhor;
        }

        assertThat(melhor).isLessThanOrEqualTo(LIMITE);
    }
}
