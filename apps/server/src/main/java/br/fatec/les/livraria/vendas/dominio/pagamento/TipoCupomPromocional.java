package br.fatec.les.livraria.vendas.dominio.pagamento;

/**
 * Tipo do cupom promocional (RF-VEN-007, DEC-11): o que ele abate e de qual base. Os de frete estendem o DRS.
 */
public enum TipoCupomPromocional {
    /** Reais abatidos dos itens. */
    VALOR_FIXO,
    /** Percentual dos itens, arredondado a 2 casas com {@code HALF_UP}. */
    PERCENTUAL,
    /** Todo o frete. */
    FRETE_GRATIS,
    /** Reais abatidos do frete. */
    FRETE_VALOR_FIXO
}
