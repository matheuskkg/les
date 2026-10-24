package br.fatec.les.livraria.clientes.aplicacao.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.cartao;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comCartoes;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.padrao;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** RN-CLI-005: todo cartão deve usar uma bandeira registrada no sistema. */
class BandeiraRegistradaTest {

    private static final Map<Long, Bandeira> BANDEIRAS = Map.of(1L, new Bandeira(1L, "Visa"));

    private final BandeiraRegistrada regra = new BandeiraRegistrada();

    @Test
    void rnCli005_bandeiraRegistradaNaoTemViolacao() {
        assertThat(violacoes(padrao())).isEmpty();
    }

    @Test
    void rnCli005_bandeiraDesconhecidaViolaARegra() {
        NovoCartao desconhecido = new NovoCartao(cartao().numero(), cartao().nomeImpresso(), 99L,
                cartao().validade());

        assertThat(violacoes(comCartoes(List.of(desconhecido))))
                .singleElement()
                .extracting(Violacao::codigo)
                .isEqualTo("RN-CLI-005");
    }

    @Test
    void rnCli005_semCartoesNaoTemViolacao() {
        assertThat(violacoes(comCartoes(List.of()))).isEmpty();
    }

    private List<Violacao> violacoes(NovoCliente alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes, BANDEIRAS);
        return violacoes.lista();
    }
}
