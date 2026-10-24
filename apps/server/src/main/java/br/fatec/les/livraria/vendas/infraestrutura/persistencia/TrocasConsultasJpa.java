package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.Especificacoes;
import br.fatec.les.livraria.vendas.aplicacao.consulta.TrocaResumo;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocasConsultas;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.infraestrutura.persistencia.TrocaJpaRepository.UnidadesDaTroca;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA da consulta de trocas do admin (RF-VEN-013, DEC-07). A página traz o pedido de cada troca para a cópia
 * do cliente; as unidades são somadas numa segunda consulta, sem carregar os itens na paginação.
 */
@Component
@RequiredArgsConstructor
public class TrocasConsultasJpa implements TrocasConsultas {

    private final TrocaJpaRepository trocas;

    @Override
    @Transactional(readOnly = true)
    public Pagina<TrocaResumo> listar(List<StatusTroca> status, Paginacao paginacao) {
        Specification<TrocaJpa> filtro = Especificacoes.em("status", status);
        Page<TrocaJpa> pagina = trocas.findAll(filtro, ConversorDePaginacao.paraPageable(paginacao));
        Map<Long, Long> unidades = pagina.isEmpty() ? Map.of()
                : trocas.somarUnidades(pagina.map(TrocaJpa::getId).getContent()).stream()
                        .collect(Collectors.toMap(UnidadesDaTroca::getTrocaId, UnidadesDaTroca::getUnidades));
        return ConversorDePaginacao.paraPagina(pagina, troca -> new TrocaResumo(troca.getId(),
                troca.getPedido().getId(), troca.getStatus(), troca.getSolicitadaEm(), troca.getPedido().getCliente(),
                unidades.getOrDefault(troca.getId(), 0L)));
    }
}
