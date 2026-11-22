package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RecomendacoesRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adapter JPA das recomendações da vitrine (RNF-REC-001, DEC-03, DEC-14). Uma recomendação nova é inserida com os
 * itens; numa existente, só a avaliação dos itens muda.
 */
@Component
@RequiredArgsConstructor
public class RecomendacoesRepositorioJpa implements RecomendacoesRepositorio {

    private final RecomendacaoJpaRepository repositorio;
    private final ChamadaIaJpaRepository chamadas;

    @Override
    @Transactional(readOnly = true)
    public Optional<Recomendacao> maisRecente(Long clienteId) {
        return repositorio.findFirstByClienteIdOrderByGeradaEmDescIdDesc(clienteId).map(this::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Recomendacao> buscarPorItem(Long itemId) {
        return repositorio.buscarPorItem(itemId).map(this::paraDominio);
    }

    @Override
    @Transactional
    public Recomendacao salvar(Recomendacao recomendacao) {
        RecomendacaoJpa entidade = recomendacao.id() == null ? nova(recomendacao) : avaliada(recomendacao);
        return paraDominio(repositorio.save(entidade));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvaliacaoDada> avaliacoesDoCliente(Long clienteId) {
        Map<Long, AvaliacaoDada> ultimas = new LinkedHashMap<>();
        for (ItemRecomendacaoJpa item : repositorio.avaliadosDoCliente(clienteId)) {
            ultimas.remove(item.getLivroId());
            ultimas.put(
                    item.getLivroId(), new AvaliacaoDada(item.getLivroId(), item.getAvaliacao() == Avaliacao.POSITIVA));
        }
        return List.copyOf(ultimas.values());
    }

    private RecomendacaoJpa nova(Recomendacao recomendacao) {
        RecomendacaoJpa entidade = new RecomendacaoJpa();
        entidade.setClienteId(recomendacao.clienteId());
        entidade.setGeradaEm(recomendacao.geradaEm());
        entidade.setComprasConsideradas(recomendacao.comprasConsideradas());
        if (recomendacao.chamadaIaId() != null) {
            entidade.setChamadaIa(chamadas.getReferenceById(recomendacao.chamadaIaId()));
        }
        for (ItemDaRecomendacao item : recomendacao.itens()) {
            ItemRecomendacaoJpa itemJpa = new ItemRecomendacaoJpa();
            itemJpa.setRecomendacao(entidade);
            itemJpa.setLivroId(item.livroId());
            itemJpa.setPosicao(item.posicao());
            itemJpa.setJustificativa(item.justificativa());
            entidade.getItens().add(itemJpa);
        }
        return entidade;
    }

    private RecomendacaoJpa avaliada(Recomendacao recomendacao) {
        RecomendacaoJpa entidade =
                repositorio
                        .findById(recomendacao.id())
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Recomendação não encontrada."));
        Map<Long, ItemDaRecomendacao> porId =
                recomendacao.itens().stream()
                        .filter(item -> item.id() != null)
                        .collect(Collectors.toMap(ItemDaRecomendacao::id, Function.identity()));
        for (ItemRecomendacaoJpa itemJpa : entidade.getItens()) {
            ItemDaRecomendacao item = porId.get(itemJpa.getId());
            if (item != null) {
                itemJpa.setAvaliacao(item.avaliacao());
                itemJpa.setAvaliadaEm(item.avaliadaEm());
            }
        }
        return entidade;
    }

    private Recomendacao paraDominio(RecomendacaoJpa entidade) {
        return new Recomendacao(
                entidade.getId(),
                entidade.getClienteId(),
                entidade.getChamadaIa() == null ? null : entidade.getChamadaIa().getId(),
                entidade.getGeradaEm(),
                entidade.getComprasConsideradas(),
                entidade.getItens().stream()
                        .map(item ->
                                new ItemDaRecomendacao(
                                        item.getId(),
                                        item.getLivroId(),
                                        item.getPosicao(),
                                        item.getJustificativa(),
                                        item.getAvaliacao(),
                                        item.getAvaliadaEm()))
                        .toList());
    }
}
