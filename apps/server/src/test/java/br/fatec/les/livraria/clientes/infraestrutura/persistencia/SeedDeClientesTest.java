package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

/** Seed do CLI-01 em H2 (RF-CLI-001, RNF-CLI-003, RN-CLI-005, DEC-10, DEC-14, DEC-16). */
@SpringBootTest
@Transactional
class SeedDeClientesTest {

    private static final String SENHA = "Senha@123";
    /** 2026-10-24T12:00 em America/Sao_Paulo, o instante citado no comentário do seed 201_clientes.sql. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    /** Ids dos usuários dos clientes do seed: 2 a 6 (Ana a Elisa) e 7 a 11 (Fernanda a Juliana), DEC-14. */
    private static final List<Long> USUARIOS_DOS_CLIENTES = List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

    /** Relógio fixo: a situação de Carla e Diego depende do instante, então o teste não pode usar o relógio real. */
    @TestConfiguration
    static class RelogioFixo {

        @Bean
        @Primary
        Relogio relogioFixo() {
            return () -> AGORA;
        }
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ClienteJpaRepository clientes;

    @Autowired
    private BandeiraJpaRepository bandeiras;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private CodificadorDeSenha senhas;

    private JdbcClient jdbc;

    @BeforeEach
    void configurar() {
        jdbc = JdbcClient.create(dataSource);
    }

    @Test
    void seedCarregaOsClientesOsCartoesEAsBandeiras() {
        assertThat(clientes.count()).isEqualTo(10);
        assertThat(bandeiras.count()).isEqualTo(5);
        assertThat(jdbc.sql("select count(*) from cartao").query(Long.class).single()).isEqualTo(10L);
        assertThat(jdbc.sql("select count(*) from endereco").query(Long.class).single()).isEqualTo(12L);
    }

    @Test
    void senhaDeTodosOsClientesDoSeedEhSenha123_RNF_CLI_003() {
        for (Long usuarioId : USUARIOS_DOS_CLIENTES) {
            String hash = usuarios.buscarPorId(usuarioId).orElseThrow().getSenhaHash();
            assertThat(hash).startsWith("$2");
            assertThat(senhas.confere(SENHA, hash)).as("senha do usuário %s", usuarioId).isTrue();
        }
    }

    @Test
    void administradoresDeApoioTemPapelAdminESenhaForte_RNF_CLI_003() {
        for (Long usuarioId : List.of(100L, 101L)) {
            var administrador = usuarios.buscarPorId(usuarioId).orElseThrow();
            assertThat(administrador.getPapel()).isEqualTo(Papel.ADMIN);
            assertThat(administrador.getSenhaHash()).startsWith("$2");
            assertThat(senhas.confere("Admin@123", administrador.getSenhaHash()))
                    .as("senha do administrador %s", usuarioId)
                    .isTrue();
        }
    }

    @Test
    void contaInativaContinuaEmUsoEContaEncerradaNaoEhEncontrada_DEC_10() {
        // Carla foi inativada há 4 dias: dentro do prazo de reativação, a conta continua em uso.
        assertThat(usuarios.buscarPorLogin("carla.mendes@livraria.test")).isPresent();
        // Diego foi inativado há mais de 7 dias: a conta está encerrada e não é encontrada pelo login.
        assertThat(usuarios.buscarPorLogin("diego.rocha@livraria.test")).isEmpty();
    }
}
