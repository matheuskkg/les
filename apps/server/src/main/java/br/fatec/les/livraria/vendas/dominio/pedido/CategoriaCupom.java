package br.fatec.les.livraria.vendas.dominio.pedido;

/**
 * Categoria do cupom usado no pedido (DEC-11, DEC-15.2). Promocionais e cupons de troca ficam em tabelas diferentes, e
 * os ids podem coincidir: só a categoria diz de onde veio o id da cópia.
 */
public enum CategoriaCupom {

    PROMOCIONAL,
    TROCA
}
