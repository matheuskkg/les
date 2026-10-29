package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.MetricasIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDeChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Leituras das métricas da IA (RNF-REC-001, DEC-03). As consultas só filtram e projetam; a agregação, a média e o
 * p95 ficam no domínio, para valerem igual no H2 e no PostgreSQL.
 */
@Component
@RequiredArgsConstructor
public class MetricasIaRepositorioJpa implements MetricasIaRepositorio {

    private static final String PROJECAO_DA_CHAMADA = "select new "
            + "br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa(c.provedor.id, c.modelo, c.operacao, "
            + "c.iniciadaEm, c.latenciaMs, c.tentativa, c.resultado, c.statusHttp, c.erro, c.tokensEntrada, "
            + "c.tokensSaida, c.chamadasHttp, c.tokensDasVoltas) from ChamadaIaJpa c ";
    private static final String PROJECAO_DA_AVALIACAO = "select new "
            + "br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDeChamada(c.provedor.id, a.avaliacao) ";

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<ChamadaIa> chamadas(LocalDateTime de, LocalDateTime ate) {
        return entityManager.createQuery(PROJECAO_DA_CHAMADA
                        + "where c.iniciadaEm >= :de and c.iniciadaEm < :ate", ChamadaIa.class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvaliacaoDeChamada> avaliacoes(LocalDateTime de, LocalDateTime ate) {
        List<AvaliacaoDeChamada> dosItens = entityManager.createQuery(PROJECAO_DA_AVALIACAO
                        + "from ItemRecomendacaoJpa a join a.recomendacao r join r.chamadaIa c "
                        + "where a.avaliacao is not null and c.iniciadaEm >= :de and c.iniciadaEm < :ate",
                        AvaliacaoDeChamada.class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .getResultList();
        List<AvaliacaoDeChamada> dasRespostas = entityManager.createQuery(PROJECAO_DA_AVALIACAO
                        + "from MensagemConversaJpa a join a.chamadaIa c "
                        + "where a.avaliacao is not null and c.iniciadaEm >= :de and c.iniciadaEm < :ate",
                        AvaliacaoDeChamada.class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .getResultList();
        return Stream.concat(dosItens.stream(), dasRespostas.stream()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long recomendacoesSemIa(LocalDateTime de, LocalDateTime ate) {
        return entityManager.createQuery("select count(r) from RecomendacaoJpa r where r.chamadaIa is null "
                        + "and r.geradaEm >= :de and r.geradaEm < :ate", Long.class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UltimaFalha> ultimasFalhas() {
        // A falha mais recente de cada provedor; num empate de instante, a de maior id.
        List<ChamadaIaJpa> falhas = entityManager.createQuery("select c from ChamadaIaJpa c "
                        + "where c.resultado <> br.fatec.les.livraria.recomendacao.dominio.modelo"
                        + ".ResultadoDaChamada.SUCESSO and c.iniciadaEm = (select max(o.iniciadaEm) "
                        + "from ChamadaIaJpa o where o.provedor = c.provedor and o.resultado <> "
                        + "br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada.SUCESSO)",
                        ChamadaIaJpa.class)
                .getResultList();
        return falhas.stream().collect(Collectors.groupingBy(falha -> falha.getProvedor().getId(),
                        Collectors.maxBy(Comparator.comparing(ChamadaIaJpa::getId))))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entrada -> {
                    ChamadaIaJpa falha = entrada.getValue().orElseThrow();
                    return new UltimaFalha(falha.getIniciadaEm(), falha.getResultado(), falha.getStatusHttp());
                }));
    }
}
