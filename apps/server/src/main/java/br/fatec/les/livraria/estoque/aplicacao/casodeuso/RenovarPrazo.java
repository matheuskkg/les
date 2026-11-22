package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Renova o prazo de todas as reservas de um carrinho ou checkout (DEC-05.1, RN-VEN-014). O bloqueio do carrinho é
 * contado a partir do último item incluído, então um item novo estende o prazo dos demais.
 */
@Component
@RequiredArgsConstructor
public class RenovarPrazo {

    private final ReservasDeEstoque reservas;
    private final Relogio relogio;

    /**
     * @param origem carrinho ou checkout
     * @param origemId id do dono das reservas
     * @param expiraEm novo fim do bloqueio, no futuro
     * @throws IllegalArgumentException se {@code expiraEm} não estiver no futuro
     */
    @Transactional
    public void executar(OrigemReserva origem, Long origemId, LocalDateTime expiraEm) {
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(origemId, "origemId");
        Objects.requireNonNull(expiraEm, "expiraEm");
        if (!expiraEm.isAfter(relogio.agora())) {
            throw new IllegalArgumentException("A reserva deve expirar no futuro.");
        }

        for (Reserva reserva : reservas.listarPorOrigem(origem, origemId)) {
            reserva.renovarPrazo(expiraEm);
            reservas.salvar(reserva);
        }
    }
}
