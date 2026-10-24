package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.DDD;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.NUMERO;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeUsuarioRepositorio;

/** RF-CLI-001: unicidade do telefone (DDD + número) entre as contas em uso (DEC-10). */
class TelefoneUnicoTest {

    private final FakeUsuarioRepositorio usuarios = new FakeUsuarioRepositorio();
    private final TelefoneUnico regra = new TelefoneUnico(
            br.fatec.les.livraria.clientes.ClientesDeTeste.contasEmUso(usuarios));

    @Test
    void rfCli001_telefoneDeContaEmUsoViolaARegra() {
        usuarios.telefonesEmUso.add(DDD + NUMERO);

        assertThat(violacoes(padrao(), regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RF-CLI-001");
    }

    @Test
    void rfCli001_telefoneLivreNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rfCli001_telefoneComMascaraEhComparadoSoPelosDigitos() {
        usuarios.telefonesEmUso.add(DDD + NUMERO);

        assertThat(violacoes(comTelefone("98888-7001"), regra)).hasSize(1);
    }

    private static br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente comTelefone(
            String numeroComMascara) {
        var padrao = padrao();
        return new br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente(padrao.nome(),
                padrao.genero(), padrao.dataNascimento(), padrao.cpf(), padrao.email(),
                padrao.tipoTelefone(), padrao.ddd(), numeroComMascara, padrao.senha(),
                padrao.confirmacaoSenha(), padrao.enderecos(), padrao.cartoes());
    }
}
