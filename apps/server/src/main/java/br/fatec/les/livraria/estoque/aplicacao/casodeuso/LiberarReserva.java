package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import lombok.RequiredArgsConstructor;

/**
 * Libera o bloqueio de um livro de um carrinho ou checkout (DEC-15.3, DEC-16), devolvendo as unidades ao
 * disponível. A operação é idempotente: liberar um livro sem reserva não faz nada.
 */
@Component
@RequiredArgsConstructor
public class LiberarReserva {

    private final ReservasDeEstoque reservas;

    /**
     * @param origem   carrinho ou checkout
     * @param origemId id do dono da reserva
     * @param livroId  livro cujo bloqueio será liberado
     */
    @Transactional
    public void executar(OrigemReserva origem, Long origemId, Long livroId) {
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(origemId, "origemId");
        Objects.requireNonNull(livroId, "livroId");

        reservas.buscar(livroId, origem, origemId)
                .ifPresent(reserva -> reservas.remover(reserva.getId()));
    }
}
