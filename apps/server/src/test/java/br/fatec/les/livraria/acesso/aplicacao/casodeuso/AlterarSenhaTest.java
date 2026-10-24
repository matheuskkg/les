package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.dominio.regra.ConfirmacaoDeSenha;
import br.fatec.les.livraria.acesso.dominio.regra.NovaSenhaObrigatoria;
import br.fatec.les.livraria.acesso.dominio.regra.SenhaForte;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeCodificadorDeSenha;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;

/** RF-CLI-008, RNF-CLI-001, RNF-CLI-002, RNF-CLI-003: alteração de senha do usuário logado. */
class AlterarSenhaTest {

    private static final String ATUAL = "Senha@123";
    private static final String NOVA = "Nova@456";

    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final FakeCodificadorDeSenha senhas = new FakeCodificadorDeSenha();

    private AlterarSenha caso;

    @BeforeEach
    void montar() {
        usuarios.salvos.add(new Usuario(2L, EMAIL, null, "hash:" + ATUAL, Papel.CLIENTE, null));
        caso = new AlterarSenha(new NovaSenhaObrigatoria(), new SenhaForte(), new ConfirmacaoDeSenha(),
                usuarios, senhas);
    }

    @Test
    void rfCli008_trocaSenhaComAtualCorretaGravaApenasOHash() {
        caso.executar(2L, new TrocaDeSenha(ATUAL, NOVA, NOVA));

        assertThat(usuarios.salvos.getLast().getSenhaHash()).isEqualTo("hash:" + NOVA);
        assertThat(senhas.codificadas).containsExactly(NOVA);
    }

    @Test
    void rfCli008_senhaAtualIncorretaResponde422SemAlterar() {
        int salvosAntes = usuarios.salvos.size();

        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(2L, new TrocaDeSenha("Outra@123", NOVA, NOVA)), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).singleElement().satisfies(violacao -> {
            assertThat(violacao.codigo()).isEqualTo("RF-CLI-008");
            assertThat(violacao.mensagem()).isEqualTo("A senha atual está incorreta.");
        });
        assertThat(usuarios.salvos).hasSize(salvosAntes);
        assertThat(senhas.codificadas).isEmpty();
    }

    @Test
    void rnfCli001_novaSenhaFracaViolaARegra() {
        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(2L, new TrocaDeSenha(ATUAL, "fraca", "fraca")), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).extracting(Violacao::codigo).contains("RNF-CLI-001");
        assertThat(senhas.codificadas).isEmpty();
    }

    @Test
    void rnfCli001_novaSenhaEmBrancoViolaARegra() {
        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(2L, new TrocaDeSenha(ATUAL, "   ", "   ")), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).singleElement().satisfies(violacao -> {
            assertThat(violacao.codigo()).isEqualTo("RNF-CLI-001");
            assertThat(violacao.mensagem()).isEqualTo("Informe a nova senha.");
        });
    }

    @Test
    void rnfCli002_confirmacaoDiferenteViolaARegra() {
        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(2L, new TrocaDeSenha(ATUAL, NOVA, "Outra@456")), ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).singleElement()
                .extracting(Violacao::codigo)
                .isEqualTo("RNF-CLI-002");
    }

    @Test
    void rfCli008_violacoesDaSenhaAtualEDaNovaAcumulam() {
        ExcecaoDeNegocio erro = catchThrowableOfType(
                () -> caso.executar(2L, new TrocaDeSenha("Outra@123", "fraca", "diferente")),
                ExcecaoDeNegocio.class);

        assertThat(erro.getViolacoes()).extracting(Violacao::codigo)
                .contains("RF-CLI-008", "RNF-CLI-001", "RNF-CLI-002");
    }

    @Test
    void rfCli008_usuarioSemCadastroNaoEhEncontrado() {
        usuarios.salvos.clear();

        assertThat(catchThrowableOfType(
                () -> caso.executar(2L, new TrocaDeSenha(ATUAL, NOVA, NOVA)), ExcecaoDeRecursoNaoEncontrado.class))
                .hasMessage("Usuário não encontrado.");
    }
}
