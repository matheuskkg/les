package br.fatec.les.livraria.vendas.dominio.pagamento;

import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.promocional;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.reais;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.troca;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/** RF-VEN-007, RN-VEN-006, DEC-11: promocional, depois cupons de troca, depois cartões. */
class DistribuirPagamentoTest {

    @Test
    void rfVen007_semCuponsOsCartoesPagamTudo() {
        DistribuicaoDoPagamento distribuicao = DistribuirPagamento.de(
                new CuponsDaCompra(reais("40.00"), reais("12.00"), null, List.of()));

        assertThat(distribuicao.total()).isEqualTo(reais("52.00"));
        assertThat(distribuicao.descontoPromocional()).isEqualTo(reais("0"));
        assertThat(distribuicao.troco()).isEqualTo(reais("0"));
        assertThat(distribuicao.restante()).isEqualTo(reais("52.00"));
    }

    @Test
    void rnVen006_cuponsDeTrocaAcimaDoTotalGeramTroco() {
        // exemplo do DRS: compra de R$ 50,00 com cupons de R$ 20 e R$ 40 gera troco de R$ 10,00
        DistribuicaoDoPagamento distribuicao = DistribuirPagamento.de(new CuponsDaCompra(reais("38.00"),
                reais("12.00"), null, List.of(troca("T20", "20"), troca("T40", "40"))));

        assertThat(distribuicao.cuponsDeTroca()).extracting(CupomAbatido::abatido)
                .containsExactly(reais("20"), reais("30"));
        assertThat(distribuicao.troco()).isEqualTo(reais("10.00"));
        assertThat(distribuicao.restante()).isEqualTo(reais("0"));
    }

    @Test
    void rnVen005_cuponsECartaoPeloRestante() {
        // R$ 35,00 = R$ 30,00 em cupons + R$ 5,00 no cartão
        DistribuicaoDoPagamento distribuicao = DistribuirPagamento.de(new CuponsDaCompra(reais("23.00"),
                reais("12.00"), null, List.of(troca("T30", "30"))));

        assertThat(distribuicao.troco()).isEqualTo(reais("0"));
        assertThat(distribuicao.restante()).isEqualTo(reais("5.00"));
    }

    @Test
    void rnVen006_excedenteDoPromocionalSePerdeSemTroco() {
        DistribuicaoDoPagamento distribuicao = DistribuirPagamento.de(new CuponsDaCompra(reais("8.00"),
                reais("12.00"), promocional(TipoCupomPromocional.VALOR_FIXO, "15"), List.of()));

        assertThat(distribuicao.descontoPromocional()).isEqualTo(reais("8.00"));
        assertThat(distribuicao.troco()).isEqualTo(reais("0"));
        assertThat(distribuicao.restante()).isEqualTo(reais("12.00"));
    }

    @Test
    void rnVen006_promocionalAbateAntesDosCuponsDeTroca() {
        DistribuicaoDoPagamento distribuicao = DistribuirPagamento.de(new CuponsDaCompra(reais("40.00"),
                reais("12.00"), promocional(TipoCupomPromocional.FRETE_GRATIS, null), List.of(troca("T45", "45"))));

        assertThat(distribuicao.descontoPromocional()).isEqualTo(reais("12.00"));
        assertThat(distribuicao.troco()).isEqualTo(reais("5.00"));
        assertThat(distribuicao.restante()).isEqualTo(reais("0"));
    }
}
