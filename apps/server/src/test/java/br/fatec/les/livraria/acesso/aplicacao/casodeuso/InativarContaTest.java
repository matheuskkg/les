package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.EMAIL;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.relogio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

/** RF-CLI-003, DEC-10: inativação da conta pelo próprio cliente. */
class InativarContaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();

    private final List<Long> efeitos = new ArrayList<>();

    private InativarConta caso;

    @BeforeEach
    void montar() {
        caso = new InativarConta(usuarios, relogio(), new SituacaoDaConta(), List.of(efeitos::add));
    }

    @Test
    void rfCli003_contaAtivaEhInativadaNoInstanteDoRelogio() {
        usuarios.salvos.add(usuario(null));

        caso.executar(2L);

        assertThat(usuarios.salvos.getLast().getInativadoEm()).isEqualTo(AGORA);
        assertThat(efeitos).containsExactly(2L);
    }

    @Test
    void rfCli003_contaInativaDentroDoPrazoNaoEhInativadaDeNovo() {
        usuarios.salvos.add(usuario(AGORA.minusDays(4)));
        int salvosAntes = usuarios.salvos.size();

        assertThat(catchThrowableOfType(ExcecaoDeConflito.class, () -> caso.executar(2L)))
                .hasMessage("A conta já está inativa.");
        assertThat(usuarios.salvos).hasSize(salvosAntes);
        assertThat(efeitos).isEmpty();
    }

    @Test
    void rfCli003_contaEncerradaNaoEhInativadaDeNovo() {
        usuarios.salvos.add(usuario(AGORA.minusDays(8)));

        assertThat(catchThrowableOfType(ExcecaoDeConflito.class, () -> caso.executar(2L)))
                .hasMessage("A conta já está inativa.");
    }

    @Test
    void rfCli003_usuarioSemCadastroNaoEhEncontrado() {
        assertThat(catchThrowableOfType(ExcecaoDeRecursoNaoEncontrado.class, () -> caso.executar(2L)))
                .hasMessage("Usuário não encontrado.");
    }

    private static Usuario usuario(LocalDateTime inativadoEm) {
        return new Usuario(2L, EMAIL, null, "hash", Papel.CLIENTE, inativadoEm);
    }
}
