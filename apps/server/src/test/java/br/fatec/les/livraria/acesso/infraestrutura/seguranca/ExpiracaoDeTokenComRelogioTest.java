package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;

/** Emissão e validação do JWT usam o mesmo {@link Relogio} controlável dos testes E2E (DEC-08, DEC-09). */
@SpringBootTest
class ExpiracaoDeTokenComRelogioTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 1, 15, 10, 30);
    private static final Duration VALIDADE = Duration.ofHours(8);

    static class RelogioDeTeste implements Relogio {

        private final AtomicReference<LocalDateTime> agora = new AtomicReference<>(INICIO);

        @Override
        public LocalDateTime agora() {
            return agora.get();
        }

        void avancar(Duration duracao) {
            agora.updateAndGet(atual -> atual.plus(duracao));
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }
    }

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private RelogioDeTeste relogio;

    @Test
    void tokenEmitidoNoRelogioFixoExpiraQuandoORelogioAvanca() {
        Usuario admin = usuarios.buscarPorId(1L).orElseThrow();
        Instant inicio = INICIO.atZone(RelogioDoSistema.FUSO).toInstant();

        TokenDeSessao token = emissor.emitir(admin);
        Jwt jwt = jwtDecoder.decode(token.valor());

        assertThat(jwt.getIssuedAt()).isEqualTo(inicio);
        assertThat(jwt.getExpiresAt()).isEqualTo(inicio.plus(VALIDADE));

        relogio.avancar(VALIDADE.plusMinutes(2));

        assertThatThrownBy(() -> jwtDecoder.decode(token.valor()))
                .isInstanceOf(JwtValidationException.class);
    }
}
