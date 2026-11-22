package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.Objects;

/**
 * Livro comprado por um cliente num pedido que conta como venda (RNF-REC-001, DEC-03), base da co-compra. As
 * quantidades já chegam somadas por cliente e livro; a co-compra usa apenas o par cliente–livro.
 *
 * @param clienteId cliente que comprou
 * @param livroId livro comprado
 * @param quantidade unidades compradas menos as trocadas em trocas concluídas
 */
public record CompraRegistrada(Long clienteId, Long livroId, int quantidade) {

    public CompraRegistrada {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(livroId, "livroId");
    }
}
