package br.fatec.les.livraria.acesso.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

    @Test
    void alterarContatoGravaEmailNormalizadoETelefone_RF_CLI_002() {
        Usuario usuario = usuario();
        Telefone telefone = new Telefone(TipoTelefone.FIXO, "21", "988887002");

        usuario.alterarContato("  Ana.Nova@Livraria.Test  ", telefone);

        assertThat(usuario.getEmail()).isEqualTo("ana.nova@livraria.test");
        assertThat(usuario.getTelefone()).isEqualTo(telefone);
    }

    @Test
    void alterarSenhaHashTrocaOHashGravado_RF_CLI_008() {
        Usuario usuario = usuario();

        usuario.alterarSenhaHash("novo-hash");

        assertThat(usuario.getSenhaHash()).isEqualTo("novo-hash");
    }

    @Test
    void alterarSenhaHashNuloFalha_RF_CLI_008() {
        Usuario usuario = usuario();

        assertThatThrownBy(() -> usuario.alterarSenhaHash(null)).isInstanceOf(NullPointerException.class);
    }

    private static Usuario usuario() {
        return new Usuario(1L, "ana@teste.test", null, "hash", Papel.CLIENTE, null);
    }
}
