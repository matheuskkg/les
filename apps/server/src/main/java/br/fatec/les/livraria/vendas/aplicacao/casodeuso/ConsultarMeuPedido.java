package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.DetalharPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Detalhe de um pedido do cliente logado, com as cópias, os valores e as trocas (RF-CLI-005, RF-VEN-011, DEC-12).
 * Pedido de outro cliente responde 404, como o inexistente, para não revelar que o número existe.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMeuPedido {

    private final IdentificarCliente identificarCliente;
    private final PedidosConsultas pedidos;
    private final DetalharPedido detalharPedido;

    /**
     * @param id número do pedido
     * @return o pedido do cliente logado, com as trocas
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir ou for de outro cliente
     */
    @Transactional(readOnly = true)
    public PedidoDetalhado executar(Long id) {
        Objects.requireNonNull(id, "id");
        Long clienteId = identificarCliente.executar();
        return pedidos.buscar(id)
                .filter(pedido -> pedido.getClienteId().equals(clienteId))
                .map(detalharPedido::executar)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado."));
    }
}
