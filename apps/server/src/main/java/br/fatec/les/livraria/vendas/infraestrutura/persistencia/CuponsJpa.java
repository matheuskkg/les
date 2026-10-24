package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsPromocionais;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;
import lombok.RequiredArgsConstructor;

/** Leitura dos cupons promocionais e de troca (DEC-11, DEC-15.2). */
@Component
@RequiredArgsConstructor
public class CuponsJpa implements CuponsPromocionais, CuponsDeTroca {

    private final CupomPromocionalJpaRepository promocionais;
    private final UsoCupomPromocionalJpaRepository usos;
    private final CupomJpaRepository cupons;

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

    private static CupomPromocional paraDominio(CupomPromocionalJpa jpa) {
        return new CupomPromocional(jpa.getId(), jpa.getCodigo(), jpa.getTipo(), jpa.getValor(), jpa.getValidade(),
                jpa.getValorMinimo() == null ? null : Dinheiro.de(jpa.getValorMinimo()));
    }

    private static CupomTroca paraDominio(CupomJpa jpa) {
        return new CupomTroca(jpa.getId(), jpa.getClienteId(), jpa.getCodigo(), Dinheiro.de(jpa.getValor()),
                jpa.getValidade(), jpa.getSituacao(), jpa.getGeradoEm());
    }
}
