package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Livro de um pedido que conta como venda, com o cliente e o pedido, base da recomendação personalizada
 * (RNF-REC-001, DEC-03, DEC-12). Título, autores e categorias vêm da cópia do livro no pedido.
 *
 * @param pedidoId pedido do item
 * @param clienteId cliente do pedido
 * @param livroId id de origem do livro
 * @param titulo título copiado no pedido
 * @param autores autores copiados no pedido
 * @param categorias nomes das categorias copiadas no pedido
 * @param quantidade quantidade comprada menos a trocada em trocas concluídas, nunca negativa
 * @param realizadoEm data e hora da finalização do pedido
 */
public record LivroVendido(Long pedidoId, Long clienteId, Long livroId, String titulo, List<String> autores,
        List<String> categorias, int quantidade, LocalDateTime realizadoEm) {

    public LivroVendido {
        Objects.requireNonNull(pedidoId, "pedidoId");
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(livroId, "livroId");
        Objects.requireNonNull(realizadoEm, "realizadoEm");
        autores = List.copyOf(autores);
        categorias = List.copyOf(categorias);
    }
}
