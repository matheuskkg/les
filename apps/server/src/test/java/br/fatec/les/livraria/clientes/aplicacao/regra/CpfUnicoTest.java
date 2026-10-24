package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.CPF;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeClienteRepositorio;
import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;

/** RF-CLI-001: unicidade do CPF entre as contas em uso (DEC-10). */
class CpfUnicoTest {

    private final FakeClienteRepositorio clientes = new FakeClienteRepositorio();
    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final CpfUnico regra = new CpfUnico(clientes,
            br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso(usuarios));

    @Test
    void rfCli001_cpfDeContaEmUsoViolaARegra() {
        clientes.usuariosPorCpf.put(CPF, List.of(2L));
        usuarios.idsEmUso.add(2L);

        assertThat(violacoes(padrao(), regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RF-CLI-001");
    }

    @Test
    void rfCli001_cpfDeContaEncerradaEhAceito() {
        clientes.usuariosPorCpf.put(CPF, List.of(3L));

        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rfCli001_cpfLivreNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }
}
