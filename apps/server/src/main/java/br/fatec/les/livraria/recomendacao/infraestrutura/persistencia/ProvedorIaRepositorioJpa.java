package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA da cadeia de provedores (RNF-REC-001, DEC-03, DEC-14). O REC-05 usa {@link #salvar(List)} para
 * alterar a ordem e a ativação; a cadeia guarda a leitura em cache por 30 segundos.
 */
@Component
@RequiredArgsConstructor
public class ProvedorIaRepositorioJpa implements ProvedoresIaRepositorio {

    private final ProvedorIaJpaRepository repositorio;
    private final RecomendacaoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProvedorIa> listarEmOrdem() {
        return repositorio.findAllByOrderByOrdemAscIdAsc().stream()
                .map(mapper::paraDominio)
                .toList();
    }

    @Override
    @Transactional
    public List<ProvedorIa> salvar(List<ProvedorIa> provedores) {
        List<ProvedorIaJpa> entidades = provedores.stream()
                .map(mapper::paraJpa)
                .map(repositorio::save)
                .toList();
        return entidades.stream().map(mapper::paraDominio).toList();
    }
}
