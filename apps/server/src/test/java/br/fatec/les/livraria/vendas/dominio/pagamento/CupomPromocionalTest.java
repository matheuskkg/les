package br.fatec.les.livraria.vendas.dominio.pagamento;

import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.HOJE;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.promocional;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.reais;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/** RF-VEN-007, DEC-11: desconto de cada tipo, limitado à base, validade e valor mínimo. */
class CupomPromocionalTest {

    @Test
    void rfVen007_valorFixoAbateDosItensAteOSubtotal() {
        CupomPromocional cupom = promocional(TipoCupomPromocional.VALOR_FIXO, "10");

        assertThat(cupom.desconto(reais("47.88"), reais("12"))).isEqualTo(reais("10.00"));
        assertThat(cupom.desconto(reais("7.00"), reais("12"))).isEqualTo(reais("7.00"));
    }

    @Test
    void rfVen007_percentualArredondaComHalfUp() {
        CupomPromocional cupom = promocional(TipoCupomPromocional.PERCENTUAL, "15");

        // 15% de R$ 47,90 = R$ 7,185 → R$ 7,19
        assertThat(cupom.desconto(reais("47.90"), reais("12"))).isEqualTo(reais("7.19"));
    }

    @Test
    void rfVen007_cuponsDeFreteAbatemSoDoFrete() {
        assertThat(promocional(TipoCupomPromocional.FRETE_GRATIS, null).desconto(reais("47.88"), reais("14")))
                .isEqualTo(reais("14.00"));
        assertThat(promocional(TipoCupomPromocional.FRETE_VALOR_FIXO, "5").desconto(reais("47.88"), reais("14")))
                .isEqualTo(reais("5.00"));
        assertThat(promocional(TipoCupomPromocional.FRETE_VALOR_FIXO, "20").desconto(reais("47.88"), reais("14")))
                .isEqualTo(reais("14.00"));
    }

    @Test
    void rnVen007_valeAteOFimDoDiaDaValidade() {
        CupomPromocional cupom = promocional(TipoCupomPromocional.VALOR_FIXO, "10");

        assertThat(cupom.vencidoEm(HOJE)).isFalse();
        assertThat(cupom.vencidoEm(HOJE.plusDays(1))).isTrue();
    }

    @Test
    void rnVen007_valorMinimoComparadoComOSubtotal() {
        CupomPromocional cupom = new CupomPromocional(1L, "MIN", TipoCupomPromocional.PERCENTUAL,
                new BigDecimal("15"), HOJE, reais("100.00"));

        assertThat(cupom.atendeValorMinimo(reais("100.00"))).isTrue();
        assertThat(cupom.atendeValorMinimo(reais("99.99"))).isFalse();
    }
}
