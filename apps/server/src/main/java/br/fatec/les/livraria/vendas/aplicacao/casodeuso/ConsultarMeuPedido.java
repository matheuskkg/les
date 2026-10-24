package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import lombok.RequiredArgsConstructor;

/**
 * Detalhe de um pedido do cliente logado, com as cópias e os valores (RF-CLI-005, DEC-12). Pedido de outro cliente
 * responde 404, como o inexistente, para não revelar que o número existe.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMeuPedido {

    private final IdentificarCliente identificarCliente;
    private final PedidosConsultas pedidos;

    /**
     * @param id número do pedido
     * @return o pedido do cliente logado
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir ou for de outro cliente
     */
    @Transactional(readOnly = true)
    public Pedido executar(Long id) {
        Objects.requireNonNull(id, "id");
        Long clienteId = identificarCliente.executar();
        return pedidos.buscar(id)
                .filter(pedido -> pedido.getClienteId().equals(clienteId))
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado."));
    }
}
