package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.ChamadasIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter JPA do log de chamadas à IA (RNF-REC-001, DEC-03, DEC-14). Usa transação própria ({@link
 * Propagation#REQUIRES_NEW}) para que a falha continue registrada mesmo se a transação de quem chamou fizer rollback.
 */
@Component
@RequiredArgsConstructor
public class ChamadaIaRepositorioJpa implements ChamadasIaRepositorio {

    private final ChamadaIaJpaRepository repositorio;
    private final ProvedorIaJpaRepository provedores;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long registrar(ChamadaIa chamada) {
        ChamadaIaJpa entidade = new ChamadaIaJpa();
        entidade.setProvedor(provedores.getReferenceById(chamada.provedorIaId()));
        entidade.setModelo(chamada.modelo());
        entidade.setOperacao(chamada.operacao());
        entidade.setIniciadaEm(chamada.iniciadaEm());
        entidade.setLatenciaMs(chamada.latenciaMs());
        entidade.setTentativa(chamada.tentativa());
        entidade.setResultado(chamada.resultado());
        entidade.setStatusHttp(chamada.codigoHttp());
        entidade.setErro(chamada.erro());
        entidade.setTokensEntrada(chamada.tokensEntrada());
        entidade.setTokensSaida(chamada.tokensSaida());
        entidade.setChamadasHttp(chamada.chamadasHttp());
        entidade.setTokensDasVoltas(chamada.tokensDasVoltas());
        return repositorio.save(entidade).getId();
    }
}
