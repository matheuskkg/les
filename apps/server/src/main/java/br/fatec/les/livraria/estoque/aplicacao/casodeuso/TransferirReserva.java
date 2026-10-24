package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.aplicacao.regra.DisponivelParaReservar;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;
import lombok.RequiredArgsConstructor;

/**
 * Passa a reserva de um livro de um dono para outro, como do carrinho para o checkout (DEC-12, DEC-16): um
 * {@code UPDATE} de {@code origem}, {@code origem_id} e {@code expira_em} da mesma linha.
 *
 * <p>Trava o saldo do livro e confere o disponível de novo (RN-VEN-001): uma reserva já vencida, que deixou de
 * consumir estoque, só passa ao novo dono se as unidades ainda estiverem livres. Sem reserva do dono anterior, uma
 * nova é criada com a mesma conferência.</p>
 */
@Component
@RequiredArgsConstructor
public class TransferirReserva {

    private final ReservasDeEstoque reservas;
    private final DisponivelParaReservar disponivelParaReservar;
    private final Relogio relogio;

    /**
     * @param livroId      livro reservado
     * @param quantidade   unidades do item, no mínimo 1
     * @param origem       dono atual
     * @param origemId     id do dono atual
     * @param destino      novo dono
     * @param destinoId    id do novo dono
     * @param expiraEm     novo fim do bloqueio, no futuro
     * @throws ExcecaoDeNegocio com {@code RN-VEN-001} se as unidades não estiverem mais disponíveis
     * @throws IllegalArgumentException se {@code quantidade < 1} ou {@code expiraEm} não estiver no futuro
     */
    @Transactional
    public void executar(Long livroId, int quantidade, OrigemReserva origem, Long origemId, OrigemReserva destino,
            Long destinoId, LocalDateTime expiraEm) {
        Objects.requireNonNull(livroId, "livroId");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade reservada deve ser no mínimo 1.");
        }
        LocalDateTime agora = relogio.agora();
        if (!Objects.requireNonNull(expiraEm, "expiraEm").isAfter(agora)) {
            throw new IllegalArgumentException("A reserva deve expirar no futuro.");
        }
        Optional<Reserva> existente = reservas.buscar(livroId, origem, origemId);
        disponivelParaReservar.exigir(livroId, quantidade, existente, agora);
        Reserva reserva = existente
                .map(atual -> {
                    atual.transferir(destino, destinoId, quantidade, expiraEm);
                    return atual;
                })
                .orElseGet(() -> Reserva.nova(livroId, destino, destinoId, quantidade, expiraEm));
        reservas.salvar(reserva);
    }
}
