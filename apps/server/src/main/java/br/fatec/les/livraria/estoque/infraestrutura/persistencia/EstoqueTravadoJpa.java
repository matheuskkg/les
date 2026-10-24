package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.estoque.aplicacao.porta.EstoqueTravado;
import lombok.RequiredArgsConstructor;

/** Adapter JPA da linha de saldo com lock pessimista (DEC-15.3). */
@Component
@RequiredArgsConstructor
public class EstoqueTravadoJpa implements EstoqueTravado {

    private final EstoqueJpaRepository repositorio;

    @Override
    @Transactional
    public Optional<Integer> quantidadeComLock(Long livroId) {
        return repositorio.buscarPorLivroIdComLock(livroId).map(EstoqueJpa::getQuantidade);
    }

    @Override
    @Transactional
    public void gravarQuantidade(Long livroId, int quantidade) {
        EstoqueJpa estoque = repositorio.buscarPorLivroIdComLock(livroId)
                .orElseThrow(() -> new IllegalStateException("Livro sem saldo de estoque: " + livroId));
        estoque.setQuantidade(quantidade);
    }
}
