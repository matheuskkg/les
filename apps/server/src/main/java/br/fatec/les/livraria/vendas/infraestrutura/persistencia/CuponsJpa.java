package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsPromocionais;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Leitura dos cupons promocionais e de troca (DEC-11, DEC-15.2). */
@Component
@RequiredArgsConstructor
public class CuponsJpa implements CuponsPromocionais, CuponsDeTroca {

    private final CupomPromocionalJpaRepository promocionais;
    private final UsoCupomPromocionalJpaRepository usos;
    private final CupomJpaRepository cupons;
    private final PedidoJpaRepository pedidos;

    @Override
    public Optional<CupomPromocional> buscarPorCodigo(String codigo) {
        return promocionais.findByCodigo(codigo).map(CuponsJpa::paraDominio);
    }

    @Override
    public boolean usadoPeloCliente(Long cupomPromocionalId, Long clienteId) {
        return usos.existsByCupomPromocionalIdAndClienteId(cupomPromocionalId, clienteId);
    }

    @Override
    public List<CupomTroca> buscarDoCliente(Long clienteId, Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return cupons.findByClienteIdAndIdIn(clienteId, ids).stream().map(CuponsJpa::paraDominio).toList();
    }

    @Override
    public List<CupomTroca> listarNaoUsados(Long clienteId) {
        return cupons.findByClienteIdAndSituacaoOrderByGeradoEmAscIdAsc(clienteId, SituacaoCupom.DISPONIVEL).stream()
                .map(CuponsJpa::paraDominio)
                .toList();
    }

    @Override
    @Transactional
    public void registrarUso(Long cupomPromocionalId, Long clienteId, Long pedidoId) {
        UsoCupomPromocionalJpa uso = new UsoCupomPromocionalJpa();
        uso.setCupomPromocional(promocionais.getReferenceById(cupomPromocionalId));
        uso.setClienteId(clienteId);
        uso.setPedido(pedidos.getReferenceById(pedidoId));
        usos.saveAndFlush(uso);
    }

    @Override
    @Transactional
    public void removerUso(Long pedidoId) {
        usos.deleteByPedidoId(pedidoId);
    }

    @Override
    @Transactional
    public void marcarComoUsados(Collection<Long> ids) {
        mudarSituacao(ids, SituacaoCupom.USADO);
    }

    @Override
    @Transactional
    public void devolver(Collection<Long> ids) {
        mudarSituacao(ids, SituacaoCupom.DISPONIVEL);
    }

    @Override
    @Transactional
    public CupomTroca gerar(CupomTroca cupom) {
        CupomJpa jpa = new CupomJpa();
        jpa.setClienteId(cupom.clienteId());
        jpa.setCodigo(cupom.codigo());
        jpa.setValor(cupom.valor().valor());
        jpa.setValidade(cupom.validade());
        jpa.setSituacao(cupom.situacao());
        jpa.setGeradoEm(cupom.geradoEm());
        return paraDominio(cupons.saveAndFlush(jpa));
    }

    /** A versão é conferida no flush: se outra transação mudou o cupom, a gravação falha com 409 (DEC-11). */
    private void mudarSituacao(Collection<Long> ids, SituacaoCupom situacao) {
        List<CupomJpa> encontrados = cupons.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new IllegalStateException("Cupom de troca não encontrado: " + ids);
        }
        encontrados.forEach(cupom -> cupom.setSituacao(situacao));
        cupons.flush();
    }

    private static CupomPromocional paraDominio(CupomPromocionalJpa jpa) {
        return new CupomPromocional(
                jpa.getId(),
                jpa.getCodigo(),
                jpa.getTipo(),
                jpa.getValor(),
                jpa.getValidade(),
                jpa.getValorMinimo() == null ? null : Dinheiro.de(jpa.getValorMinimo()));
    }

    private static CupomTroca paraDominio(CupomJpa jpa) {
        return new CupomTroca(
                jpa.getId(),
                jpa.getClienteId(),
                jpa.getCodigo(),
                Dinheiro.de(jpa.getValor()),
                jpa.getValidade(),
                jpa.getSituacao(),
                jpa.getGeradoEm());
    }
}
