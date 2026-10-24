package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemVendido;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.PedidoJpaRepository.PedidoDoCliente;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.TrocaJpaRepository.CuponsDoPedido;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.TrocaJpaRepository.QuantidadeTrocada;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA das vendas consideradas na análise e no ranking (RN-ANA-001, RN-ANA-004, RN-CLI-007, DEC-12, DEC-16). As categorias estão no snapshot JSON do
 * livro, então a consulta traz cada item e a agregação por mês e categoria fica em Java, sem SQL de JSON, que
 * difere entre H2 e PostgreSQL. As quantidades trocadas vêm numa segunda consulta, já somadas por item.
 */
@Component
@RequiredArgsConstructor
public class VendasConsultasJpa implements VendasConsultas {

    private static final List<StatusPedido> CONTAM_COMO_VENDA = Arrays.stream(StatusPedido.values())
            .filter(StatusPedido::contaComoVenda).toList();

    private final PedidoJpaRepository pedidos;
    private final ItemPedidoJpaRepository itens;
    private final TrocaJpaRepository trocas;

    @Override
    @Transactional(readOnly = true)
    public List<ItemVendido> itensVendidos(LocalDateTime inicio, LocalDateTime fimExclusivo) {
        Map<Long, Long> trocadas = trocas.somarTrocadasDoPeriodo(StatusTroca.TROCADO, CONTAM_COMO_VENDA, inicio,
                fimExclusivo).stream()
                .collect(Collectors.toMap(QuantidadeTrocada::getItemId, QuantidadeTrocada::getQuantidade));
        return itens.buscarDoPeriodo(CONTAM_COMO_VENDA, inicio, fimExclusivo).stream()
                .map(item -> new ItemVendido(item.getRealizadoEm(), item.getLivro().categorias(),
                        Dinheiro.de(item.getPrecoUnitario()), item.getQuantidade(),
                        Math.toIntExact(trocadas.getOrDefault(item.getId(), 0L))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<CompraDoCliente>> comprasDosClientes(Collection<Long> clienteIds, LocalDateTime desde) {
        if (clienteIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, BigDecimal> cupons = trocas.somarCuponsDosClientesDesde(StatusTroca.TROCADO, clienteIds,
                CONTAM_COMO_VENDA, desde).stream()
                .collect(Collectors.toMap(CuponsDoPedido::getPedidoId, CuponsDoPedido::getValor));
        return pedidos.buscarDosClientesDesde(clienteIds, CONTAM_COMO_VENDA, desde).stream()
                .collect(Collectors.groupingBy(PedidoDoCliente::getClienteId, Collectors.mapping(
                        pedido -> new CompraDoCliente(pedido.getRealizadoEm(), Dinheiro.de(pedido.getTotal()
                                .subtract(cupons.getOrDefault(pedido.getId(), BigDecimal.ZERO)))),
                        Collectors.toList())));
    }
}
