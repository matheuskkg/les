package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.DDD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.NUMERO;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.alteracao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.junit.jupiter.api.Test;

import java.util.List;

/** RF-CLI-002, DEC-10: unicidade do telefone (DDD + número) na alteração, ignorando a própria conta. */
class TelefoneUnicoNaAlteracaoTest {

    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final TelefoneUnicoNaAlteracao regra = new TelefoneUnicoNaAlteracao(contasEmUso(usuarios));

    @Test
    void rfCli002_mesmoTelefoneDaPropriaContaNaoViolaARegra() {
        usuarios.donosDosTelefones.put(DDD + NUMERO, 2L);

        assertThat(violacoes(comTelefone(2L, DDD, NUMERO))).isEmpty();
    }

    @Test
    void rfCli002_telefoneDeOutraContaEmUsoViolaARegra() {
        usuarios.donosDosTelefones.put(DDD + NUMERO, 3L);

        assertThat(violacoes(comTelefone(2L, DDD, NUMERO)))
                .singleElement()
                .extracting(Violacao::codigo)
                .isEqualTo("RF-CLI-002");
    }

    @Test
    void rfCli002_telefoneLivreNaoTemViolacao() {
        assertThat(violacoes(comTelefone(2L, DDD, NUMERO))).isEmpty();
    }

    @Test
    void rfCli002_tipoDoTelefoneNaoEntraNaComparacao() {
        usuarios.donosDosTelefones.put(DDD + NUMERO, 3L);

        assertThat(violacoes(comTelefone(2L, DDD, NUMERO))).hasSize(1);
        assertThat(violacoes(comTelefone(2L, DDD, NUMERO))).extracting(Violacao::codigo).containsOnly("RF-CLI-002");
    }

    @Test
    void rfCli002_telefoneComMascaraEhComparadoPelosDigitos() {
        usuarios.donosDosTelefones.put("11988887001", 3L);

        assertThat(violacoes(comTelefone(2L, "(11)", "98888-7001"))).hasSize(1);
    }

    @Test
    void rfCli002_telefoneInvalidoNaoEhValidadoPorEstaRegra() {
        usuarios.telefonesEmUso.add("11123");

        assertThat(violacoes(comTelefone(2L, "1", "123"))).isEmpty();
    }

    private static DadosAlteradosDoCliente comTelefone(Long usuarioId, String ddd, String numero) {
        DadosAlteradosDoCliente padrao = alteracao();
        return new DadosAlteradosDoCliente(
                usuarioId,
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                TipoTelefone.CELULAR,
                ddd,
                numero);
    }

    private List<Violacao> violacoes(DadosAlteradosDoCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
