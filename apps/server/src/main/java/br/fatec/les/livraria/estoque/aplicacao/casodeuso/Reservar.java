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
 * Reserva unidades de um livro para um carrinho ou checkout (RN-VEN-001, RN-VEN-014, DEC-15.3). É a porta de
 * entrada pública do módulo {@code estoque} para o {@code vendas} reservar, alterar e renovar o bloqueio do
 * item na mesma transação (DEC-13).
 *
 * <p>Trava a linha de saldo com lock pessimista, confere o disponível
 * ({@code quantidade − reservas ativas}) e cria ou atualiza a reserva do dono
 * {@code (origem, origemId, livroId)}. A reserva do próprio dono não entra na conta do disponível, para que
 * alterar a quantidade não conte duas vezes as unidades já bloqueadas. Reserva vencida não consome estoque
 * (DEC-16).</p>
 */
@Component
@RequiredArgsConstructor
public class Reservar {

    private final ReservasDeEstoque reservas;
    private final DisponivelParaReservar disponivelParaReservar;
    private final Relogio relogio;

    /**
     * @param livroId    livro a reservar
     * @param quantidade quantidade total desejada para este dono, no mínimo 1
     * @param origem     carrinho ou checkout
     * @param origemId   id do carrinho ou do checkout
     * @param expiraEm   fim do bloqueio, no futuro
     * @throws ExcecaoDeNegocio com a violação {@code RN-VEN-001} quando não há unidades suficientes, informando
     *                          o disponível na mensagem
     * @throws IllegalArgumentException se {@code quantidade < 1} ou {@code expiraEm} não estiver no futuro
     */
    @Transactional
    public void executar(Long livroId, int quantidade, OrigemReserva origem, Long origemId,
            LocalDateTime expiraEm) {
        Objects.requireNonNull(livroId, "livroId");
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(origemId, "origemId");
        Objects.requireNonNull(expiraEm, "expiraEm");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade reservada deve ser no mínimo 1.");
        }

        LocalDateTime agora = relogio.agora();
        if (!expiraEm.isAfter(agora)) {
            throw new IllegalArgumentException("A reserva deve expirar no futuro.");
        }

        Optional<Reserva> existente = reservas.buscar(livroId, origem, origemId);
        disponivelParaReservar.exigir(livroId, quantidade, existente, agora);

        Reserva reserva = existente
                .map(atual -> {
                    atual.alterar(quantidade, expiraEm);
                    return atual;
                })
                .orElseGet(() -> Reserva.nova(livroId, origem, origemId, quantidade, expiraEm));
        reservas.salvar(reserva);
    }
}
