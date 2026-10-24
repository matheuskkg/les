package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pedido.CategoriaNoPedido;

/**
 * Item de um pedido que conta como venda, base da análise de vendas (RN-ANA-001, RN-ANA-004, DEC-12).
 *
 * @param realizadoEm data e hora da finalização do pedido, no fuso da loja
 * @param categorias categorias copiadas no pedido
 * @param precoUnitario preço unitário de venda
 * @param quantidade quantidade comprada
 * @param quantidadeTrocada quantidade em trocas concluídas ({@code TROCADO})
 */
public record ItemVendido(LocalDateTime realizadoEm, List<CategoriaNoPedido> categorias, Dinheiro precoUnitario,
        int quantidade, int quantidadeTrocada) {

    public ItemVendido {
        Objects.requireNonNull(realizadoEm, "realizadoEm");
        Objects.requireNonNull(precoUnitario, "precoUnitario");
        categorias = List.copyOf(categorias);
    }

    /** Preço unitário × quantidade não trocada; trocas em andamento ainda não descontam (DEC-12). */
    public Dinheiro valor() {
        return precoUnitario.multiplicar(quantidade - quantidadeTrocada);
    }
}
