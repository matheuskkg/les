package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.estoque.aplicacao.porta.EstoqueTravado;
import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import lombok.RequiredArgsConstructor;

/**
 * Baixa do estoque de uma venda aprovada (RF-EST-003, RN-EST-006, DEC-13, DEC-15.3): para cada livro, trava a linha
 * de saldo, subtrai a quantidade vendida e apaga a reserva do dono, que deixa de ser necessária. Chamado pelo
 * {@code vendas} na transação da aprovação.
 *
 * <p>Os livros são travados em ordem crescente de id, para que duas aprovações com livros em comum não se esperem
 * em ordem cruzada.</p>
 */
@Component
@RequiredArgsConstructor
public class BaixarEstoque {

    private final EstoqueTravado estoque;
    private final ReservasDeEstoque reservas;

    /**
     * @param origem      dono das reservas consumidas, como o checkout
     * @param origemId    id do dono
     * @param quantidades unidades vendidas por livro
     * @throws IllegalStateException se um livro não tiver saldo ou a baixa o deixar negativo, o que as reservas
     *                               deveriam impedir
     */
    @Transactional
    public void executar(OrigemReserva origem, Long origemId, Map<Long, Integer> quantidades) {
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(origemId, "origemId");
        new TreeMap<>(quantidades).forEach((livroId, vendida) -> {
            int emMaos = estoque.quantidadeComLock(livroId)
                    .orElseThrow(() -> new IllegalStateException("Livro sem saldo de estoque: " + livroId));
            if (vendida < 1 || vendida > emMaos) {
                throw new IllegalStateException("Baixa de %d unidades do livro %d com %d em mãos."
                        .formatted(vendida, livroId, emMaos));
            }
            estoque.gravarQuantidade(livroId, emMaos - vendida);
            reservas.buscar(livroId, origem, origemId).ifPresent(reserva -> reservas.remover(reserva.getId()));
        });
    }
}
