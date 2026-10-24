package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.estoque.aplicacao.porta.EstoqueTravado;
import lombok.RequiredArgsConstructor;

/**
 * Reentrada no estoque dos itens de uma troca que voltam a ser vendáveis (RF-EST-004, DEC-12, DEC-15.3): para cada
 * livro, trava a linha de saldo e soma a quantidade. Não cria entrada, porque não há custo novo; o histórico fica na
 * auditoria. Chamado pelo {@code vendas} na transação do recebimento da troca.
 *
 * <p>Os livros são travados em ordem crescente de id, como na baixa, para que as duas operações não se esperem em
 * ordem cruzada.</p>
 */
@Component
@RequiredArgsConstructor
public class ReentrarEstoque {

    private final EstoqueTravado estoque;

    /**
     * @param quantidades unidades que voltam ao estoque, por livro
     * @throws IllegalStateException se um livro não tiver linha de saldo ou a quantidade não for positiva
     */
    @Transactional
    public void executar(Map<Long, Integer> quantidades) {
        new TreeMap<>(quantidades).forEach((livroId, devolvida) -> {
            int emMaos = estoque.quantidadeComLock(livroId)
                    .orElseThrow(() -> new IllegalStateException("Livro sem saldo de estoque: " + livroId));
            if (devolvida < 1) {
                throw new IllegalStateException("Reentrada de %d unidades do livro %d.".formatted(devolvida, livroId));
            }
            estoque.gravarQuantidade(livroId, emMaos + devolvida);
        });
    }
}
