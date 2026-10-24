package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.DetalharPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Detalhe de um pedido para o administrador (RF-VEN-009, RF-VEN-010, DEC-12), com as cópias de itens, cliente,
 * endereços, cartões, cupons e valores, e as trocas (RF-VEN-011). Pedido inexistente responde HTTP 404.
 */
@Component
@RequiredArgsConstructor
public class ConsultarPedido {

    private final PedidosConsultas pedidos;
    private final DetalharPedido detalharPedido;

    /**
     * @param id número do pedido
     * @return o pedido com todas as cópias e as trocas
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir
     */
    @Transactional(readOnly = true)
    public PedidoDetalhado executar(Long id) {
        Objects.requireNonNull(id, "id");
        return detalharPedido.executar(pedidos.buscar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado.")));
    }
}
