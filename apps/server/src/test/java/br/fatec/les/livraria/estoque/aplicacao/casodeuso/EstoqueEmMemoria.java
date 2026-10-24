package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.util.Map;
import java.util.Optional;

import br.fatec.les.livraria.estoque.aplicacao.porta.EstoqueTravado;

/** Saldo em memória para os testes dos casos de uso do estoque; o lock não tem efeito aqui. */
final class EstoqueEmMemoria implements EstoqueTravado {

    private final Map<Long, Integer> quantidades;

    EstoqueEmMemoria(Map<Long, Integer> quantidades) {
        this.quantidades = quantidades;
    }

    @Override
    public Optional<Integer> quantidadeComLock(Long livroId) {
        return Optional.ofNullable(quantidades.get(livroId));
    }

    @Override
    public void gravarQuantidade(Long livroId, int quantidade) {
        quantidades.put(livroId, quantidade);
    }
}
