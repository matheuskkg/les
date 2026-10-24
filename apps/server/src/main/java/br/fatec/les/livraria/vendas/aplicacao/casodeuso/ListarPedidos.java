package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.BuscarClientesPorNomeOuCpf;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ConsultaDePedidos;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FiltroPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoResumo;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PeriodoDoPedido;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Consulta de pedidos do administrador (RF-VEN-009, RF-VEN-010, DEC-07, DEC-12), ponto de partida do despacho e da
 * entrega. Nome e CPF do cliente vivem no módulo {@code clientes}: este caso de uso pede os ids dos clientes e os
 * entrega à porta de consulta, que filtra {@code pedido.cliente_id IN (...)}. O período é informado em dias e o
 * último entra inteiro.
 */
@Component
@RequiredArgsConstructor
public class ListarPedidos {

    private final PedidosConsultas pedidos;
    private final BuscarClientesPorNomeOuCpf buscarClientes;

    /**
     * @param consulta filtros informados pelo admin; não pode ser nula
     * @param paginacao página, tamanho e ordenação já validados; não pode ser nula
     * @return a página de pedidos
     * @throws br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido se o período for invertido ou o CPF
     *         não tiver dígitos
     */
    @Transactional(readOnly = true)
    public Pagina<PedidoResumo> executar(ConsultaDePedidos consulta, Paginacao paginacao) {
        Objects.requireNonNull(consulta, "consulta");
        Objects.requireNonNull(paginacao, "paginacao");
        PeriodoDoPedido periodo = PeriodoDoPedido.dosDias(consulta.realizadoEmDe(), consulta.realizadoEmAte());
        FiltroPedido filtro = new FiltroPedido(consulta.numero(), consulta.clienteId(), consulta.status(),
                periodo.inicio(), periodo.fimExclusivo(), buscarClientes.executar(consulta.cliente(), consulta.cpf()));
        return pedidos.listar(filtro, paginacao);
    }
}
