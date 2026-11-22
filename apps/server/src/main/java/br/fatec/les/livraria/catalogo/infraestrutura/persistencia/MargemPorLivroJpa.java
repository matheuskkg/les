package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import br.fatec.les.livraria.catalogo.aplicacao.porta.MargemPorLivro;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/** Adapter JPA da porta {@link MargemPorLivro} (RN-LIV-003, RN-EST-002). */
@Component
@RequiredArgsConstructor
public class MargemPorLivroJpa implements MargemPorLivro {

    private final LivroJpaRepository livros;

    @Override
    public Map<Long, BigDecimal> buscar(Collection<Long> livroIds) {
        return livros.buscarMargensPorLivro(livroIds).stream()
                .collect(Collectors.toMap(MargemDoLivroProjecao::getLivroId, MargemDoLivroProjecao::getMargem));
    }
}
