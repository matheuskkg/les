package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.ClientesDeTeste.FakeBandeiraRepositorio;

/** RN-CLI-005: todo cartão deve usar uma bandeira registrada no sistema. */
class BandeiraRegistradaTest {

    private final FakeBandeiraRepositorio bandeiras = new FakeBandeiraRepositorio().comBandeiras(1L);
    private final BandeiraRegistrada regra = new BandeiraRegistrada(bandeiras);

    @Test
    void rnCli005_bandeiraRegistradaNaoTemViolacao() {
        assertThat(violacoes(padrao(), regra)).isEmpty();
    }

    @Test
    void rnCli005_bandeiraDesconhecidaViolaARegra() {
        var desconhecido = new br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao(
                cartao().numero(), cartao().nomeImpresso(), 99L, cartao().validade());

        assertThat(violacoes(comCartao(desconhecido), regra))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RN-CLI-005");
    }

    @Test
    void rnCli005_semCartoesNaoTemViolacao() {
        var padrao = padrao();
        var semCartoes = new br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente(
                padrao.nome(), padrao.genero(), padrao.dataNascimento(), padrao.cpf(), padrao.email(),
                padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), padrao.senha(),
                padrao.confirmacaoSenha(), padrao.enderecos(), List.of());

        assertThat(violacoes(semCartoes, regra)).isEmpty();
    }

    private static br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente comCartao(
            br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao cartao) {
        var padrao = padrao();
        return new br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente(padrao.nome(),
                padrao.genero(), padrao.dataNascimento(), padrao.cpf(), padrao.email(),
                padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), padrao.senha(),
                padrao.confirmacaoSenha(), padrao.enderecos(), List.of(cartao));
    }
}
