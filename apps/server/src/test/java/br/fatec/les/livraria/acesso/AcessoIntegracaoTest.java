package br.fatec.les.livraria.acesso;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ListarAdministradores;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

/**
 * Integração do módulo {@code acesso} com o H2 e o seed (RNF-CLI-003, DEC-10, DEC-13, DEC-14). Usa um banco próprio
 * porque os demais contextos de teste compartilham o mesmo H2 e a tabela {@code seed_execucao} sobrevive à recriação
 * do schema, o que faria o seed ser pulado.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:acesso;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
        + "DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1")
class AcessoIntegracaoTest {

    @Autowired
    private UsuarioRepositorio usuarios;

    @Autowired
    private CodificadorDeSenha senhas;

    @Autowired
    private ListarAdministradores listarAdministradores;

    @Autowired
    private Relogio relogio;

    @Test
    void administradorDoSeedExisteComASenhaAdmin123_RNF_CLI_003() {
        Usuario admin = usuarios.buscarPorLogin("admin@livraria.test").orElseThrow();

        assertThat(admin.getId()).isEqualTo(1L);
        assertThat(admin.getPapel()).isEqualTo(Papel.ADMIN);
        assertThat(admin.getSenhaHash()).startsWith("$2");
        assertThat(senhas.confere("Admin@123", admin.getSenhaHash())).isTrue();
    }

    @Test
    void administradorDoSeedEntraPeloTelefoneComMascara_DEC_10() {
        assertThat(usuarios.buscarPorLogin("(11) 90000-0000"))
                .get()
                .extracting(Usuario::getId)
                .isEqualTo(1L);
    }

    @Test
    void listarAdministradoresRetornaOAdminDoSeed_DEC_13() {
        assertThat(listarAdministradores.executar()).containsExactly(1L);
    }

    @Test
    void contaInativaContinuaEmUsoNoLoginENaUnicidade_DEC_10() {
        usuarios.salvar(new Usuario(null, "inativa@livraria.test",
                new Telefone(TipoTelefone.FIXO, "11", "33334444"),
                senhas.codificar("Senha@123"), Papel.CLIENTE, relogio.agora().minusDays(6)));

        assertThat(usuarios.buscarPorLogin("(11) 3333-4444")).isPresent();
        assertThat(usuarios.existeEmailEmUso("inativa@livraria.test")).isTrue();
        assertThat(usuarios.existeTelefoneEmUso("1133334444")).isTrue();
    }

    @Test
    void contaEncerradaNaoEhEncontradaNoLoginNemNaUnicidade_DEC_10() {
        usuarios.salvar(new Usuario(null, "encerrada@livraria.test",
                new Telefone(TipoTelefone.CELULAR, "11", "955556666"),
                senhas.codificar("Senha@123"), Papel.CLIENTE, relogio.agora().minusDays(8)));

        assertThat(usuarios.buscarPorLogin("encerrada@livraria.test")).isEmpty();
        assertThat(usuarios.buscarPorLogin("(11) 95555-6666")).isEmpty();
        assertThat(usuarios.existeEmailEmUso("encerrada@livraria.test")).isFalse();
        assertThat(usuarios.existeTelefoneEmUso("11955556666")).isFalse();
    }

    @Test
    void novoUsuarioRecebeIdDaSequenceAcimaDaFaixaDoSeed_DEC_14() {
        Usuario novo = new Usuario(null, "cliente.novo@livraria.test",
                new Telefone(TipoTelefone.CELULAR, "11", "988887777"),
                senhas.codificar("Senha@123"), Papel.CLIENTE, null);

        Usuario salvo = usuarios.salvar(novo);

        assertThat(salvo.getId()).isGreaterThanOrEqualTo(10000L);
        assertThat(usuarios.buscarPorId(salvo.getId())).isPresent();
    }
}
