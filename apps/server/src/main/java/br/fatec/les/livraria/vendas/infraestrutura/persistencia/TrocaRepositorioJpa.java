package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.troca.ItemTroca;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA da troca (RF-VEN-011, DEC-12, DEC-16). Os itens são gravados na criação; depois dela mudam o status, o
 * motivo da recusa, o valor e o cupom gerado, e se cada item volta ao estoque.
 */
@Component
@RequiredArgsConstructor
public class TrocaRepositorioJpa implements TrocaRepositorio {

    private final TrocaJpaRepository repositorio;
    private final PedidoJpaRepository pedidos;
    private final ItemPedidoJpaRepository itensDoPedido;
    private final CupomJpaRepository cupons;

    @Override
    @Transactional
    public Troca salvar(Troca troca) {
        TrocaJpa entidade =
                troca.getId() == null
                        ? nova(troca)
                        : repositorio
                                .findById(troca.getId())
                                .orElseThrow(() -> new IllegalStateException("Troca não encontrada: " + troca.getId()));
        entidade.setStatus(troca.getStatus());
        entidade.setMotivoRecusa(troca.getMotivoRecusa());
        entidade.setValorCupom(troca.getValorCupom() == null ? null : troca.getValorCupom().valor());
        entidade.setCupom(troca.getCupomId() == null ? null : cupons.getReferenceById(troca.getCupomId()));
        for (ItemTrocaJpa linha : entidade.getItens()) {
            troca.getItens().stream()
                    .filter(item -> linha.getId() != null && linha.getId().equals(item.id()))
                    .findFirst()
                    .ifPresent(item -> linha.setVoltaEstoque(item.voltaEstoque()));
        }
        return paraDominio(repositorio.saveAndFlush(entidade));
    }

    @Override
    @Transactional
    public Optional<Troca> buscarParaAlterar(Long id) {
        return repositorio.buscarPorIdComLock(id).map(TrocaRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Troca> listarDoPedido(Long pedidoId) {
        return repositorio.findByPedidoId(pedidoId).stream().map(TrocaRepositorioJpa::paraDominio).toList();
    }

    private TrocaJpa nova(Troca troca) {
        TrocaJpa entidade = new TrocaJpa();
        entidade.setPedido(pedidos.getReferenceById(troca.getPedidoId()));
        entidade.setSolicitadaEm(troca.getSolicitadaEm());
        for (ItemTroca item : troca.getItens()) {
            ItemTrocaJpa linha = new ItemTrocaJpa();
            linha.setTroca(entidade);
            linha.setItemPedido(itensDoPedido.getReferenceById(item.itemPedidoId()));
            linha.setQuantidade(item.quantidade());
            linha.setVoltaEstoque(item.voltaEstoque());
            entidade.getItens().add(linha);
        }
        return entidade;
    }

    static Troca paraDominio(TrocaJpa entidade) {
        return new Troca(
                entidade.getId(),
                entidade.getPedido().getId(),
                entidade.getStatus(),
                entidade.getSolicitadaEm(),
                entidade.getItens().stream()
                        .map(item ->
                                new ItemTroca(
                                        item.getId(),
                                        item.getItemPedido().getId(),
                                        item.getQuantidade(),
                                        item.getVoltaEstoque()))
                        .toList(),
                entidade.getMotivoRecusa(),
                entidade.getValorCupom() == null ? null : Dinheiro.de(entidade.getValorCupom()),
                entidade.getCupom() == null ? null : entidade.getCupom().getId());
    }
}
