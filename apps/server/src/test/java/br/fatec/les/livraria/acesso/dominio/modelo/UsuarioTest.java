package br.fatec.les.livraria.acesso.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 23, 12, 0);

    @Test
    void inativarGuardaADataDaInativacao_RF_CLI_003() {
        Usuario usuario = usuario();

        usuario.inativar(AGORA);

        assertThat(usuario.getInativadoEm()).isEqualTo(AGORA);
    }

    @Test
    void inativarContaJaInativaFalha_RF_CLI_003() {
        Usuario usuario = usuario();
        usuario.inativar(AGORA);

        assertThatThrownBy(() -> usuario.inativar(AGORA.plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está inativa");
    }

    @Test
    void reativarLimpaADataDaInativacao_RF_CLI_003() {
        Usuario usuario = usuario();
        usuario.inativar(AGORA);

        usuario.reativar();

        assertThat(usuario.getInativadoEm()).isNull();
    }

    @Test
    void reativarContaAtivaFalha_RF_CLI_003() {
        Usuario usuario = usuario();

        assertThatThrownBy(usuario::reativar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está ativa");
    }

    @Test
    void emailEhNormalizadoEmMinusculas() {
        Usuario usuario = new Usuario(1L, "  Ana.Teste@Livraria.Test  ", null, "hash", Papel.CLIENTE, null);

        assertThat(usuario.getEmail()).isEqualTo("ana.teste@livraria.test");
    }

    private static Usuario usuario() {
        return new Usuario(1L, "ana@teste.test", null, "hash", Papel.CLIENTE, null);
    }
}
