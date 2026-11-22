package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/** Adapter JPA da porta {@link MaiorCustoPorLivro} (RN-EST-002, DEC-15.3). */
@Component
@RequiredArgsConstructor
public class MaiorCustoPorLivroJpa implements MaiorCustoPorLivro {

    private final EntradaEstoqueJpaRepository entradas;

    @Override
    public Map<Long, BigDecimal> buscar(Collection<Long> livroIds) {
        return entradas.buscarMaiorCustoPorLivro(livroIds).stream()
                .collect(
                        Collectors.toMap(
                                MaiorCustoDoLivroProjecao::getLivroId, MaiorCustoDoLivroProjecao::getMaiorCusto));
    }
}
