package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import lombok.RequiredArgsConstructor;

/**
 * Detalhe de um pedido para o administrador (RF-VEN-009, RF-VEN-010, DEC-12), com as cópias de itens, cliente,
 * endereços, cartões, cupons e valores. Pedido inexistente responde HTTP 404.
 */
@Component
@RequiredArgsConstructor
public class ConsultarPedido {

    private final PedidosConsultas pedidos;

    /**
     * @param id número do pedido
     * @return o pedido com todas as cópias
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir
     */
    @Transactional(readOnly = true)
    public Pedido executar(Long id) {
        Objects.requireNonNull(id, "id");
        return pedidos.buscar(id).orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado."));
    }
}
