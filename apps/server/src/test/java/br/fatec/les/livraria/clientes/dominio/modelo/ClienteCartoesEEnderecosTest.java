package br.fatec.les.livraria.clientes.dominio.modelo;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.cliente;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.clientes.dominio.regra.RemocaoDoUltimoEndereco;

/** RF-CLI-006, RF-CLI-007, DEC-10: invariantes de endereços e do cartão preferencial no agregado do cliente. */
class ClienteCartoesEEnderecosTest {

    private static final Bandeira VISA = new Bandeira(1L, "Visa");

    @Test
    void rfCli007_primeiroCartaoViraPreferencialEOsDemaisNao() {
        Cliente comDois = cliente(1L, 2L).comCartao(cartao(10L)).comCartao(cartao(11L));

        assertThat(comDois.cartao(10L).orElseThrow().preferencial()).isTrue();
        assertThat(comDois.cartao(11L).orElseThrow().preferencial()).isFalse();
    }

    @Test
    void rfCli007_marcarPreferencialDeixaUmSo() {
        Cliente cliente = cliente(1L, 2L).comCartao(cartao(10L)).comCartao(cartao(11L))
                .comCartaoPreferencial(11L);

        assertThat(cliente.getCartoes()).filteredOn(Cartao::preferencial)
                .singleElement()
                .extracting(Cartao::id)
                .isEqualTo(11L);
    }

    @Test
    void rfCli007_excluirOPreferencialPromoveOMaisRecente() {
        Cliente cliente = cliente(1L, 2L).comCartao(cartao(10L)).comCartao(cartao(12L)).comCartao(cartao(11L))
                .semCartao(10L);

        assertThat(cliente.getCartoes()).filteredOn(Cartao::preferencial)
                .singleElement()
                .extracting(Cartao::id)
                .isEqualTo(12L);
    }

    @Test
    void rfCli007_excluirCartaoNaoPreferencialMantemOPreferencial() {
        Cliente cliente = cliente(1L, 2L).comCartao(cartao(10L)).comCartao(cartao(11L)).semCartao(11L);

        assertThat(cliente.cartao(10L).orElseThrow().preferencial()).isTrue();
    }

    @Test
    void rfCli007_alterarCartaoPreservaAPreferencia() {
        Cliente cliente = cliente(1L, 2L).comCartao(cartao(10L));
        Cartao alterado = new Cartao(10L, "4111111111111111", "ANA S SOUZA", VISA, YearMonth.of(2031, 1), false);

        assertThat(cliente.comCartaoAtualizado(alterado).cartao(10L).orElseThrow().preferencial()).isTrue();
    }

    @Test
    void rfCli007_cartaoDeOutroClienteNaoEhEncontrado() {
        assertThatThrownBy(() -> cliente(1L, 2L).semCartao(99L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rnCli001_naoRemoveOUnicoEndereco() {
        assertThat(violacoes(cliente(1L, 2L), new RemocaoDoUltimoEndereco()))
                .singleElement()
                .extracting(violacao -> violacao.codigo())
                .isEqualTo("RN-CLI-001");
    }

    private static Cartao cartao(Long id) {
        return new Cartao(id, "4111111111111111", "ANA SOUZA", VISA, YearMonth.of(2030, 12), false);
    }
}
