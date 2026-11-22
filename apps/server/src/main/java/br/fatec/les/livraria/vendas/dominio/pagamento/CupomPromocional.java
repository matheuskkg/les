package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Cupom promocional, criado pelo seed (RF-VEN-007, DEC-11, DEC-15.2). Vale até o fim do dia da validade, pode exigir um
 * valor mínimo de compra (subtotal dos itens, sem frete) e cada cliente o usa uma vez.
 *
 * @param valor reais ou percentual, conforme o tipo; nulo em {@code FRETE_GRATIS}
 * @param valorMinimo subtotal mínimo dos itens; nulo se não houver
 */
public record CupomPromocional(
        Long id, String codigo, TipoCupomPromocional tipo, BigDecimal valor, LocalDate validade, Dinheiro valorMinimo) {

    private static final BigDecimal CEM = new BigDecimal("100");

    public CupomPromocional {
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(validade, "validade");
        if (tipo != TipoCupomPromocional.FRETE_GRATIS) {
            Objects.requireNonNull(valor, "valor");
        }
    }

    /** Vencido a partir do dia seguinte à validade (DEC-11). */
    public boolean vencidoEm(LocalDate hoje) {
        return hoje.isAfter(validade);
    }

    public boolean atendeValorMinimo(Dinheiro subtotal) {
        return valorMinimo == null || !subtotal.menorQue(valorMinimo);
    }

    /**
     * Desconto na compra, limitado à base do tipo (itens ou frete): o que passa da base se perde, porque é desconto e
     * não crédito (DEC-11).
     */
    public Dinheiro desconto(Dinheiro subtotal, Dinheiro frete) {
        return switch (tipo) {
            case VALOR_FIXO -> Dinheiro.menor(Dinheiro.de(valor), subtotal);
            case PERCENTUAL -> Dinheiro.menor(subtotal.multiplicar(valor.divide(CEM)), subtotal);
            case FRETE_GRATIS -> frete;
            case FRETE_VALOR_FIXO -> Dinheiro.menor(Dinheiro.de(valor), frete);
        };
    }
}
