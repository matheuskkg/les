package br.fatec.les.livraria.vendas.dominio.pedido;

import java.util.Objects;

/**
 * Cópia do cartão no pedido: só bandeira e 4 últimos dígitos (DEC-10, DEC-11, DEC-16). Número completo e código de
 * segurança nunca são gravados.
 *
 * @param id cartão do cadastro de origem; nulo para um cartão novo não salvo no cadastro
 */
public record CartaoNoPedido(Long id, String bandeira, String ultimosDigitos) {

    public CartaoNoPedido {
        Objects.requireNonNull(bandeira, "bandeira");
        Objects.requireNonNull(ultimosDigitos, "ultimosDigitos");
    }
}
