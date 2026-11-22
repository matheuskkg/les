package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDoLivro;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/** Adapter JPA da porta {@link SaldoDeEstoque} (DEC-15.3). */
@Component
@RequiredArgsConstructor
public class SaldoDeEstoqueJpa implements SaldoDeEstoque {

    private final EstoqueJpaRepository estoques;

    @Override
    public Map<Long, SaldoDoLivro> saldos(Collection<Long> livroIds, LocalDateTime agora) {
        return estoques.buscarSaldos(livroIds, agora).stream()
                .collect(
                        Collectors.toMap(
                                SaldoDoLivroProjecao::getLivroId,
                                linha -> new SaldoDoLivro(linha.getQuantidade(), linha.getReservada())));
    }
}
