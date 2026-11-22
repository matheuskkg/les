package br.fatec.les.livraria.clientes.aplicacao.porta;

import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Compras dos clientes para o ranking (RN-CLI-007, DEC-04, DEC-12). Os pedidos são do módulo {@code vendas}, que
 * implementa esta porta; assim o {@code clientes} não depende dele.
 */
public interface HistoricoDeCompras {

    /**
     * @param clienteIds clientes desejados
     * @param desde início do período, inclusive
     * @return as compras de cada cliente que contam como venda, com o valor já sem os cupons das trocas concluídas;
     *     cliente sem compras pode faltar no mapa
     */
    Map<Long, List<CompraDoCliente>> comprasDesde(Collection<Long> clienteIds, LocalDateTime desde);
}
