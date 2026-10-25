package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.Objects;

/**
 * Livro comprado por um cliente num pedido que conta como venda (RNF-REC-001, DEC-03), base da co-compra e dos mais
 * vendidos.
 *
 * @param clienteId  cliente que comprou
 * @param livroId    livro comprado
 * @param quantidade quantidade líquida das trocas concluídas
 */
public record CompraRegistrada(Long clienteId, Long livroId, int quantidade) {

    public CompraRegistrada {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(livroId, "livroId");
    }
}
