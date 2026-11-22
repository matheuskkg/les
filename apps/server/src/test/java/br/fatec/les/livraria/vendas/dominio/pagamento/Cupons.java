package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Cupons para os testes do pagamento (DEC-11). */
public final class Cupons {

    public static final LocalDate HOJE = LocalDate.of(2026, 10, 24);

    private Cupons() {}

    public static CupomTroca troca(String codigo, String valor) {
        return new CupomTroca(
                1L,
                1L,
                codigo,
                Dinheiro.de(valor),
                null,
                SituacaoCupom.DISPONIVEL,
                LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    public static CupomPromocional promocional(TipoCupomPromocional tipo, String valor) {
        return new CupomPromocional(1L, "PROMO", tipo, valor == null ? null : new BigDecimal(valor), HOJE, null);
    }

    public static Dinheiro reais(String valor) {
        return Dinheiro.de(valor);
    }
}
