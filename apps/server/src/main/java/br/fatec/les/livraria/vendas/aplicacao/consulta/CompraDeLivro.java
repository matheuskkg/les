package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.Objects;

/**
 * Livro comprado por um cliente em pedidos que contam como venda, com a quantidade líquida das trocas concluídas
 * já somada por par cliente–livro–cópia do livro no pedido (RNF-REC-001, DEC-03, DEC-12): base da co-compra da
 * recomendação, que usa apenas o par cliente–livro. O agrupamento é pela cópia em JSON do snapshot (DEC-16), então
 * cópias divergentes do mesmo livro — caso teórico enquanto a edição de livros está fora do MVP — sairiam em
 * registros distintos do mesmo par; quem consome, a co-compra ({@code ContarCoCompras}), mapeia para conjunto por
 * cliente e é imune a essa multiplicidade.
 *
 * @param clienteId  cliente que comprou
 * @param livroId     livro comprado, lido da cópia no pedido
 * @param quantidade  unidades compradas menos as trocadas em trocas concluídas
 */
public record CompraDeLivro(Long clienteId, Long livroId, int quantidade) {

    public CompraDeLivro {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(livroId, "livroId");
    }
}
