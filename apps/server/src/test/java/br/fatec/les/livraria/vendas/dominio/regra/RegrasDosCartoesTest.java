package br.fatec.les.livraria.vendas.dominio.regra;

import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.reais;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CartoesDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.PagamentoEmCartao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** RN-VEN-004, RN-VEN-005, RN-CLI-004, DEC-11: valor, quantidade e código de segurança dos cartões no pagamento. */
class RegrasDosCartoesTest {

    @Test
    void rnVen004_variosCartoesDeAoMenosDezReaisFechandoORestante() {
        CartoesDaCompra cartoes =
                new CartoesDaCompra(reais("52.00"), false, List.of(cartao("4111", "10.00"), cartao("5555", "42.00")));

        assertThat(violacoes(new ValorMinimoPorCartao(), cartoes)).isEmpty();
        assertThat(violacoes(new SomaDosCartoes(), cartoes)).isEmpty();
        assertThat(violacoes(new CartaoRepetido(), cartoes)).isEmpty();
        assertThat(violacoes(new ValorInformadoDoCartao(), cartoes)).isEmpty();
    }

    @Test
    void rnVen004_cartaoAbaixoDeDezReaisEhRecusado() {
        CartoesDaCompra cartoes =
                new CartoesDaCompra(reais("52.00"), false, List.of(cartao("4111", "9.99"), cartao("5555", "42.01")));

        assertThat(violacoes(new ValorMinimoPorCartao(), cartoes))
                .extracting(Violacao::codigo)
                .containsExactly("RN-VEN-004");
    }

    @Test
    void rnVen004_semCupomORestanteAbaixoDeDezNaoTemExcecao() {
        CartoesDaCompra cartoes = new CartoesDaCompra(reais("5.00"), false, List.of(cartao("4111", "5.00")));

        assertThat(violacoes(new ValorMinimoPorCartao(), cartoes))
                .extracting(Violacao::codigo)
                .containsExactly("RN-VEN-004");
    }

    @Test
    void rnVen005_comCupomUmCartaoPagaORestanteAbaixoDeDez() {
        CartoesDaCompra umCartao = new CartoesDaCompra(reais("5.00"), true, List.of(cartao("4111", "5.00")));
        CartoesDaCompra doisCartoes =
                new CartoesDaCompra(reais("5.00"), true, List.of(cartao("4111", "2.50"), cartao("5555", "2.50")));

        assertThat(violacoes(new ValorMinimoPorCartao(), umCartao)).isEmpty();
        assertThat(violacoes(new ValorMinimoPorCartao(), doisCartoes))
                .extracting(Violacao::codigo)
                .containsExactly("RN-VEN-005");
    }

    @Test
    void rnVen004_somaDiferenteDoRestanteOuSemCartaoEhRecusada() {
        SomaDosCartoes regra = new SomaDosCartoes();

        assertThat(violacoes(regra, new CartoesDaCompra(reais("52.00"), false, List.of(cartao("4111", "50.00")))))
                .singleElement()
                .satisfies(violacao -> assertThat(violacao.mensagem()).contains("R$ 52,00"));
        assertThat(violacoes(regra, new CartoesDaCompra(reais("52.00"), false, List.of()))).hasSize(1);
        assertThat(violacoes(regra, new CartoesDaCompra(reais("0"), true, List.of(cartao("4111", "10.00")))))
                .hasSize(1);
        assertThat(violacoes(regra, new CartoesDaCompra(reais("0"), true, List.of()))).isEmpty();
    }

    @Test
    void rnVen004_mesmoCartaoDuasVezesEhRecusado() {
        CartoesDaCompra cartoes =
                new CartoesDaCompra(reais("40.00"), false, List.of(cartao("4111", "20.00"), cartao("4111", "20.00")));

        assertThat(violacoes(new CartaoRepetido(), cartoes)).hasSize(1);
    }

    @Test
    void rnVen004_valorComCentavosFracionadosOuNaoPositivoEhRecusado() {
        ValorInformadoDoCartao regra = new ValorInformadoDoCartao();

        assertThat(violacoes(regra, new CartoesDaCompra(reais("10.00"), false, List.of(cartao("4111", "9.995")))))
                .hasSize(1);
        assertThat(violacoes(regra, new CartoesDaCompra(reais("10.00"), false, List.of(cartao("4111", "0")))))
                .hasSize(1);
        assertThat(
                        violacoes(
                                regra,
                                new CartoesDaCompra(
                                        reais("10.00"), false, List.of(new PagamentoEmCartao("4111", null)))))
                .hasSize(1);
        assertThat(violacoes(regra, new CartoesDaCompra(reais("10.00"), false, List.of(cartao("4111", "10.000")))))
                .isEmpty();
    }

    @Test
    void rnCli004_codigoDeSegurancaComTresOuQuatroDigitosEmCadaCartao() {
        assertThat(violacoes(new CodigosDeSeguranca(), List.of("123", "1234"))).isEmpty();
        assertThat(violacoes(new CodigosDeSeguranca(), Arrays.asList("12", null, "12a", "12345")))
                .extracting(Violacao::codigo)
                .containsExactly("RN-CLI-004", "RN-CLI-004", "RN-CLI-004", "RN-CLI-004");
    }

    private static PagamentoEmCartao cartao(String numero, String valor) {
        return new PagamentoEmCartao(numero, new BigDecimal(valor));
    }

    private static <T> List<Violacao> violacoes(Regra<T> regra, T alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
