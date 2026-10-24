package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.clientes.ClientesDeTeste;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

/**
 * Consulta em lote dos contatos usados pela listagem de clientes do admin (RF-CLI-004, DEC-07, DEC-10).
 */
class ConsultarUsuariosTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    private final ClientesDeTeste.FakeUsuarioRepositorio usuarios = new ClientesDeTeste.FakeUsuarioRepositorio();
    private final Relogio relogio = () -> AGORA;

    private ConsultarUsuarios consultar;

    @BeforeEach
    void criar() {
        consultar = new ConsultarUsuarios(usuarios, new SituacaoDaConta(), relogio);
    }

    @Test
    void rfCli004_carregaOsContatosEmLoteECalculaASituacaoDeCadaConta() {
        Usuario ativa = usuarios.salvar(usuario("ana@livraria.test", "11988887001", null));
        Usuario inativa = usuarios.salvar(usuario("carla@livraria.test", "31988887003",
                AGORA.minusDays(4)));
        Usuario encerrada = usuarios.salvar(usuario("diego@livraria.test", "41988887004",
                AGORA.minusDays(8)));

        Map<Long, ContatoDoUsuario> contatos = consultar.executar(
                List.of(ativa.getId(), inativa.getId(), encerrada.getId()));

        assertThat(contatos).hasSize(3);
        assertThat(contatos.get(ativa.getId()).situacao()).isEqualTo(SituacaoConta.ATIVA);
        assertThat(contatos.get(inativa.getId()).situacao()).isEqualTo(SituacaoConta.INATIVA);
        assertThat(contatos.get(encerrada.getId()).situacao()).isEqualTo(SituacaoConta.ENCERRADA);
        assertThat(contatos.get(ativa.getId()).email()).isEqualTo("ana@livraria.test");
        assertThat(contatos.get(ativa.getId()).telefone().digitos()).isEqualTo("11988887001");
    }

    @Test
    void rfCli004_semIdsNaoConsultaOsUsuarios() {
        assertThat(consultar.executar(Set.of())).isEmpty();
    }

    @Test
    void rfCli004_administradorSemTelefoneFicaComTelefoneNulo_DEC_10() {
        Usuario admin = usuarios.salvar(new Usuario(null, "admin@livraria.test", null, "hash",
                Papel.ADMIN, null));

        ContatoDoUsuario contato = consultar.executar(List.of(admin.getId())).get(admin.getId());

        assertThat(contato.telefone()).isNull();
        assertThat(contato.situacao()).isEqualTo(SituacaoConta.ATIVA);
    }

    private static Usuario usuario(String email, String telefone, LocalDateTime inativadoEm) {
        return new Usuario(null, email,
                new Telefone(TipoTelefone.CELULAR, telefone.substring(0, 2), telefone.substring(2)),
                "hash", Papel.CLIENTE, inativadoEm);
    }
}
