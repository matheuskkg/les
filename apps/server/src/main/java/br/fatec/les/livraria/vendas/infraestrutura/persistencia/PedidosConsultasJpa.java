package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FiltroPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoResumo;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter JPA da consulta de pedidos do admin (RF-VEN-009, DEC-07). A listagem usa as {@link EspecificacoesDePedido} e
 * lê só as colunas do pedido e a cópia do cliente; itens, cartões e cupons são carregados apenas no detalhe, sem trava.
 */
@Component
@RequiredArgsConstructor
public class PedidosConsultasJpa implements PedidosConsultas {

    private final PedidoJpaRepository pedidos;

    @Override
    @Transactional(readOnly = true)
    public Pagina<PedidoResumo> listar(FiltroPedido filtro, Paginacao paginacao) {
        Page<PedidoJpa> pagina =
                pedidos.findAll(EspecificacoesDePedido.de(filtro), ConversorDePaginacao.paraPageable(paginacao));
        return ConversorDePaginacao.paraPagina(pagina, PedidosConsultasJpa::paraResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<StatusPedido, Long> contarPorStatus() {
        return pedidos.contarPorStatus().stream()
                .collect(Collectors.toMap(QuantidadePorStatus::getStatus, QuantidadePorStatus::getQuantidade));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscar(Long id) {
        return pedidos.buscarPorIdComItens(id).map(PedidoRepositorioJpa::paraDominio);
    }

    private static PedidoResumo paraResumo(PedidoJpa pedido) {
        return new PedidoResumo(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getRealizadoEm(),
                pedido.getCliente(),
                Dinheiro.de(pedido.getTotal()));
    }
}
