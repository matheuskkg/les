package br.fatec.les.livraria.vendas.aplicacao.consulta;

import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Filtro da porta de consulta de pedidos do admin (RF-VEN-009, DEC-07). Os campos se combinam com E. Nome e CPF vêm do
 * módulo {@code clientes} já resolvidos em ids: um {@link Optional} vazio não filtra, e um conjunto vazio significa que
 * nenhum cliente atendeu ao filtro.
 *
 * @param numero número do pedido; nulo não filtra
 * @param clienteId código do cliente; nulo não filtra
 * @param status status desejados, combinados com OU; vazio não filtra
 * @param situacaoDaTroca status da troca mais recente do pedido, combinados com OU; vazio não filtra (DEC-12)
 * @param realizadoDesde início do período da finalização, inclusive; nulo não filtra
 * @param realizadoAntesDe fim do período da finalização, exclusive; nulo não filtra
 * @param clienteIds ids dos clientes que atendem a nome e/ou CPF
 */
public record FiltroPedido(
        Long numero,
        Long clienteId,
        List<StatusPedido> status,
        List<StatusTroca> situacaoDaTroca,
        LocalDateTime realizadoDesde,
        LocalDateTime realizadoAntesDe,
        Optional<Set<Long>> clienteIds) {

    public FiltroPedido {
        status = status == null ? List.of() : status.stream().filter(Objects::nonNull).toList();
        situacaoDaTroca =
                situacaoDaTroca == null ? List.of() : situacaoDaTroca.stream().filter(Objects::nonNull).toList();
        clienteIds = clienteIds == null ? Optional.empty() : clienteIds.map(Set::copyOf);
    }
}
