package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.acesso.aplicacao.CredenciaisInvalidas;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Tabela de login do DEC-15.4, com reativação de até 7 dias (DEC-10) e login por e-mail ou telefone. */
class AutenticarTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 23, 12, 0);
    private static final String SENHA = "Senha@123";
    private static final String HASH = "hash-de-" + SENHA;
    private static final String MENSAGEM_INVALIDO = "Login ou senha inválidos.";

    private final RepositorioEmMemoria usuarios = new RepositorioEmMemoria();
    private final CodificadorFake senhas = new CodificadorFake();
    private final RelogioFixo relogio = new RelogioFixo(AGORA);

    private Autenticar autenticar;

    @BeforeEach
    void criar() {
        autenticar = new Autenticar(usuarios, senhas, relogio, new SituacaoDaConta());
    }

    @Test
    void autenticaContaAtivaComSenhaCorreta() {
        Usuario ana = usuarios.adicionar(usuario(1L, "ana@teste.test", null, null));

        Usuario autenticado = autenticar.executar(new Autenticar.Credenciais("ana@teste.test", SENHA));

        assertThat(autenticado).isSameAs(ana);
        assertThat(usuarios.salvamentos()).isZero();
    }

    @Test
    void autenticaContaInativaEReativa_RF_CLI_003() {
        Usuario ana = usuarios.adicionar(usuario(1L, "ana@teste.test", null, AGORA.minusDays(1)));

        Usuario autenticado = autenticar.executar(new Autenticar.Credenciais("ana@teste.test", SENHA));

        assertThat(autenticado).isSameAs(ana);
        assertThat(autenticado.getInativadoEm()).isNull();
        assertThat(usuarios.salvamentos()).isEqualTo(1);
    }

    @Test
    void contaInativaReativaNoLimiteDeSeteDias_RF_CLI_003() {
        usuarios.adicionar(usuario(1L, "ana@teste.test", null, AGORA.minusDays(7)));

        Usuario autenticado = autenticar.executar(new Autenticar.Credenciais("ana@teste.test", SENHA));

        assertThat(autenticado.getInativadoEm()).isNull();
    }

    @Test
    void recusaContaEncerradaMesmoComSenhaCorreta_DEC_15_4() {
        Usuario ana = usuarios.adicionar(usuario(1L, "ana@teste.test", null, AGORA.minusDays(8)));

        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais("ana@teste.test", SENHA)))
                .isInstanceOf(CredenciaisInvalidas.class)
                .hasMessage(MENSAGEM_INVALIDO);

        assertThat(ana.getInativadoEm()).isEqualTo(AGORA.minusDays(8));
        assertThat(usuarios.salvamentos()).isZero();
        assertThat(senhas.conferencias()).isEqualTo(1);
    }

    @Test
    void recusaSenhaErradaNaContaAtiva_DEC_15_4() {
        usuarios.adicionar(usuario(1L, "ana@teste.test", null, null));

        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais("ana@teste.test", "outra-senha")))
                .isInstanceOf(CredenciaisInvalidas.class)
                .hasMessage(MENSAGEM_INVALIDO);
    }

    @Test
    void recusaSenhaErradaNaContaInativaSemReativar_DEC_15_4() {
        Usuario ana = usuarios.adicionar(usuario(1L, "ana@teste.test", null, AGORA.minusDays(1)));

        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais("ana@teste.test", "outra-senha")))
                .isInstanceOf(CredenciaisInvalidas.class)
                .hasMessage(MENSAGEM_INVALIDO);

        assertThat(ana.getInativadoEm()).isEqualTo(AGORA.minusDays(1));
        assertThat(usuarios.salvamentos()).isZero();
    }

    @Test
    void recusaLoginDesconhecido_DEC_15_4() {
        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais("ninguem@teste.test", SENHA)))
                .isInstanceOf(CredenciaisInvalidas.class)
                .hasMessage(MENSAGEM_INVALIDO);
        // O hash fictício é conferido mesmo sem conta, para o tempo não revelar que o login não existe.
        assertThat(senhas.conferencias()).isEqualTo(1);
    }

    @Test
    void recusaCredenciaisNulas_DEC_15_4() {
        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais(null, SENHA)))
                .isInstanceOf(CredenciaisInvalidas.class);
        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais("ana@teste.test", null)))
                .isInstanceOf(CredenciaisInvalidas.class);
    }

    @Test
    void autenticaPorTelefoneComMascara_DEC_10() {
        usuarios.adicionar(usuario(1L, "ana@teste.test", new Telefone(TipoTelefone.CELULAR, "11", "900000000"), null));

        Usuario autenticado = autenticar.executar(new Autenticar.Credenciais("(11) 90000-0000", SENHA));

        assertThat(autenticado.getId()).isEqualTo(1L);
    }

    @Test
    void autenticaPorTelefoneComCodigoDoPais_DEC_10() {
        usuarios.adicionar(usuario(1L, "ana@teste.test", new Telefone(TipoTelefone.CELULAR, "11", "988887777"), null));

        Usuario autenticado = autenticar.executar(new Autenticar.Credenciais("+55 (11) 98888-7777", SENHA));

        assertThat(autenticado.getId()).isEqualTo(1L);
    }

    @Test
    void loginPorTelefoneNaoEncontraUsuarioSemTelefone_DEC_10() {
        usuarios.adicionar(usuario(1L, "admin@teste.test", null, null));

        assertThatThrownBy(() -> autenticar.executar(new Autenticar.Credenciais("(11) 90000-0000", SENHA)))
                .isInstanceOf(CredenciaisInvalidas.class)
                .hasMessage(MENSAGEM_INVALIDO);
    }

    private static Usuario usuario(Long id, String email, Telefone telefone, LocalDateTime inativadoEm) {
        return new Usuario(id, email, telefone, HASH, Papel.CLIENTE, inativadoEm);
    }

    private static final class RelogioFixo implements Relogio {

        private final LocalDateTime agora;

        private RelogioFixo(LocalDateTime agora) {
            this.agora = agora;
        }

        @Override
        public LocalDateTime agora() {
            return agora;
        }
    }

    private static final class CodificadorFake implements CodificadorDeSenha {

        private int conferencias;

        private int conferencias() {
            return conferencias;
        }

        @Override
        public String codificar(String senha) {
            return "hash-de-" + senha;
        }

        @Override
        public boolean confere(String senha, String senhaHash) {
            conferencias++;
            return senhaHash.equals(codificar(senha));
        }
    }

    private static final class RepositorioEmMemoria implements UsuarioRepositorio {

        private final Map<Long, Usuario> porId = new LinkedHashMap<>();
        private int salvamentos;

        private Usuario adicionar(Usuario usuario) {
            porId.put(usuario.getId(), usuario);
            return usuario;
        }

        private int salvamentos() {
            return salvamentos;
        }

        @Override
        public Usuario salvar(Usuario usuario) {
            porId.put(usuario.getId(), usuario);
            salvamentos++;
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public List<Usuario> buscarPorIds(java.util.Collection<Long> ids) {
            return ids.stream().map(porId::get).filter(java.util.Objects::nonNull).toList();
        }

        @Override
        public java.util.Set<Long> buscarIdsPorContato(String email, String telefone) {
            return java.util.Set.of();
        }

        @Override
        public java.util.Set<Long> listarIdsPorSituacao(
                java.util.Collection<br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta> situacoes,
                java.time.LocalDateTime agora) {
            return java.util.Set.of();
        }

        @Override
        public Optional<Usuario> buscarPorLogin(String login) {
            if (login == null || login.isBlank()) {
                return Optional.empty();
            }
            String identificador = login.strip();
            return porId.values().stream().filter(usuario -> combina(usuario, identificador)).findFirst();
        }

        @Override
        public boolean existeEmailEmUso(String email) {
            return email != null
                    && porId.values().stream().anyMatch(usuario -> usuario.getEmail().equalsIgnoreCase(email.strip()));
        }

        @Override
        public boolean existeEmailEmUsoExceto(String email, Long usuarioId) {
            return email != null
                    && porId.values().stream()
                            .filter(usuario -> !usuario.getId().equals(usuarioId))
                            .anyMatch(usuario -> usuario.getEmail().equalsIgnoreCase(email.strip()));
        }

        @Override
        public boolean existeTelefoneEmUso(String telefone) {
            String digitos = somenteDigitos(telefone);
            return porId.values().stream()
                    .anyMatch(usuario ->
                            usuario.getTelefone() != null && usuario.getTelefone().digitos().equals(digitos));
        }

        @Override
        public boolean existeTelefoneEmUsoExceto(String telefone, Long usuarioId) {
            String digitos = somenteDigitos(telefone);
            return porId.values().stream()
                    .filter(usuario -> !usuario.getId().equals(usuarioId))
                    .anyMatch(usuario ->
                            usuario.getTelefone() != null && usuario.getTelefone().digitos().equals(digitos));
        }

        @Override
        public boolean existeAlgumaEmUso(java.util.Collection<Long> ids) {
            return ids.stream().anyMatch(porId::containsKey);
        }

        @Override
        public List<Long> listarAdministradores() {
            return porId.values().stream()
                    .filter(usuario -> usuario.getPapel() == Papel.ADMIN)
                    .map(Usuario::getId)
                    .toList();
        }

        private static boolean combina(Usuario usuario, String login) {
            if (login.contains("@")) {
                return usuario.getEmail().equalsIgnoreCase(login);
            }
            String digitos = somenteDigitos(login);
            return usuario.getTelefone() != null && usuario.getTelefone().digitos().equals(digitos);
        }

        private static String somenteDigitos(String telefone) {
            String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
            return digitos.length() == 13 && digitos.startsWith("55") ? digitos.substring(2) : digitos;
        }
    }
}
