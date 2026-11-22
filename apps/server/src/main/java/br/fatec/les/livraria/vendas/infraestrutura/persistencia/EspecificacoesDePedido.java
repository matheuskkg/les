package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.Especificacoes;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FiltroPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Traduz o {@link FiltroPedido} para as {@link Specification} da consulta de pedidos do admin (RF-VEN-009, DEC-07). Os
 * campos se combinam com E; status repetidos combinam com OU.
 */
final class EspecificacoesDePedido {

    private EspecificacoesDePedido() {}

    static Specification<PedidoJpa> de(FiltroPedido filtro) {
        Objects.requireNonNull(filtro, "filtro");
        return Specification.<PedidoJpa>unrestricted()
                .and(Especificacoes.igual("id", filtro.numero()))
                .and(Especificacoes.igual("clienteId", filtro.clienteId()))
                .and(Especificacoes.em("status", filtro.status()))
                .and(comSituacaoDaTroca(filtro.situacaoDaTroca()))
                .and(desde(filtro.realizadoDesde()))
                .and(antesDe(filtro.realizadoAntesDe()))
                .and(porCliente(filtro.clienteIds()));
    }

    /**
     * Pedido cuja troca mais recente (maior número, como no indicador do {@code TrocasDoPedido}) está numa das
     * situações (DEC-12).
     */
    private static Specification<PedidoJpa> comSituacaoDaTroca(List<StatusTroca> situacoes) {
        if (situacoes.isEmpty()) {
            return Specification.unrestricted();
        }
        return (raiz, consulta, cb) -> {
            Subquery<Long> maisRecente = consulta.subquery(Long.class);
            Root<TrocaJpa> outra = maisRecente.from(TrocaJpa.class);
            maisRecente.select(cb.max(outra.get("id"))).where(cb.equal(outra.get("pedido"), raiz));
            Subquery<Long> naSituacao = consulta.subquery(Long.class);
            Root<TrocaJpa> troca = naSituacao.from(TrocaJpa.class);
            naSituacao
                    .select(troca.get("id"))
                    .where(cb.equal(troca.get("id"), maisRecente), troca.get("status").in(situacoes));
            return cb.exists(naSituacao);
        };
    }

    private static Specification<PedidoJpa> desde(LocalDateTime inicio) {
        if (inicio == null) {
            return Specification.unrestricted();
        }
        return (raiz, consulta, cb) -> cb.greaterThanOrEqualTo(raiz.get("realizadoEm"), inicio);
    }

    private static Specification<PedidoJpa> antesDe(LocalDateTime fim) {
        if (fim == null) {
            return Specification.unrestricted();
        }
        return (raiz, consulta, cb) -> cb.lessThan(raiz.get("realizadoEm"), fim);
    }

    /** Filtro resolvido no {@code clientes}: ausente não filtra; vazio não encontra nenhum pedido. */
    private static Specification<PedidoJpa> porCliente(Optional<Set<Long>> ids) {
        if (ids.isEmpty()) {
            return Specification.unrestricted();
        }
        Set<Long> valores = ids.get();
        return valores.isEmpty() ? Especificacoes.nenhum() : Especificacoes.em("clienteId", valores);
    }
}
