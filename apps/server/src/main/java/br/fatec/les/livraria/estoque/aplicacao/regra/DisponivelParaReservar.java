package br.fatec.les.livraria.estoque.aplicacao.regra;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.estoque.aplicacao.porta.EstoqueTravado;
import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Confere se há unidades livres para reservar (RN-VEN-001, DEC-15.3). Trava a linha de saldo do livro, que serializa as
 * reservas concorrentes, e calcula o disponível sem a reserva atual do dono, para que alterar ou transferir não conte
 * duas vezes as unidades já bloqueadas. Reserva vencida não consome estoque (DEC-16).
 */
@Component
@RequiredArgsConstructor
public class DisponivelParaReservar {

    private final EstoqueTravado estoqueTravado;
    private final ReservasDeEstoque reservas;
    private final CalcularDisponivel calcularDisponivel;

    /**
     * Deve ser chamada dentro da transação que grava a reserva, para que a trava valha até o commit.
     *
     * @param livroId livro a reservar
     * @param quantidade quantidade total desejada
     * @param atual reserva atual do dono, se houver
     * @param agora instante usado para considerar uma reserva ativa
     * @throws ExcecaoDeNegocio com {@code RN-VEN-001} e o disponível na mensagem, se faltar estoque
     */
    public void exigir(Long livroId, int quantidade, Optional<Reserva> atual, LocalDateTime agora) {
        int emMaos = estoqueTravado.quantidadeComLock(livroId).orElse(0);
        int reservadoPorOutros =
                reservas.quantidadeReservadaAtivaExceto(livroId, agora, atual.map(Reserva::getId).orElse(-1L));
        int disponivel = calcularDisponivel.calcular(emMaos, reservadoPorOutros);
        if (quantidade > disponivel) {
            throw new ExcecaoDeNegocio(
                    "RN-VEN-001",
                    "Estoque insuficiente para o livro %d. Disponível: %d.".formatted(livroId, disponivel));
        }
    }
}
