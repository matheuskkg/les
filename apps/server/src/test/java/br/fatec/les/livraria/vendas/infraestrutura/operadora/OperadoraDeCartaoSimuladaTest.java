package br.fatec.les.livraria.vendas.infraestrutura.operadora;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pagamento.CobrancaNoCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.RespostaDaOperadora;

class OperadoraDeCartaoSimuladaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 24);
    private final OperadoraDeCartaoSimulada operadora = new OperadoraDeCartaoSimulada();

    @Test
    void rnVen007_aprovaCartaoValidoComQualquerOutroCodigo() {
        RespostaDaOperadora resposta = cobrar("4111111111111111", YearMonth.of(2026, 10), "123");

        assertThat(resposta.aprovada()).isTrue();
        assertThat(resposta.motivo()).isNull();
    }

    @Test
    void rnVen007_recusaCartaoVencidoNaDataDaTransacao() {
        assertThat(cobrar("4111111111111111", YearMonth.of(2026, 9), "123").motivo()).isEqualTo("Cartão vencido");
    }

    @Test
    void rnVen007_recusaNumeroTerminadoEmQuatroZeros() {
        assertThat(cobrar("4111111111110000", YearMonth.of(2030, 1), "123").motivo())
                .isEqualTo("Transação recusada pela operadora");
    }

    @Test
    void rnVen007_recusaCodigoDeSegurancaZerado() {
        assertThat(cobrar("4111111111111111", YearMonth.of(2030, 1), "000").motivo())
                .isEqualTo("Código de segurança inválido");
    }

    @Test
    void rnVen007_condicoesSaoAvaliadasNaOrdemDoDec052() {
        assertThat(cobrar("4111111111110000", YearMonth.of(2026, 9), "000").motivo()).isEqualTo("Cartão vencido");
        assertThat(cobrar("4111111111110000", YearMonth.of(2030, 1), "000").motivo())
                .isEqualTo("Transação recusada pela operadora");
    }

    @Test
    void rnVen007_cobrancaNaoMostraNumeroNemCodigo() {
        CobrancaNoCartao cobranca = new CobrancaNoCartao("4111111111111234", YearMonth.of(2030, 1), "987",
                Dinheiro.de("10.00"));

        assertThat(cobranca.toString()).contains("1234").doesNotContain("41111111").doesNotContain("987");
    }

    private RespostaDaOperadora cobrar(String numero, YearMonth validade, String codigo) {
        return operadora.cobrar(new CobrancaNoCartao(numero, validade, codigo, Dinheiro.de("10.00")), HOJE);
    }
}
