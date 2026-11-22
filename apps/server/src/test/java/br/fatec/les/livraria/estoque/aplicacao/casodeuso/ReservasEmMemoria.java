package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reservas em memória para os testes dos casos de uso do estoque. */
final class ReservasEmMemoria implements ReservasDeEstoque {

    final List<Reserva> todas = new ArrayList<>();
    private long proximoId = 1;

    @Override
    public Optional<Reserva> buscar(Long livroId, OrigemReserva origem, Long origemId) {
        return todas.stream()
                .filter(reserva ->
                        reserva.getLivroId().equals(livroId)
                                && reserva.getOrigem() == origem
                                && reserva.getOrigemId().equals(origemId))
                .findFirst();
    }

    @Override
    public List<Reserva> listarPorOrigem(OrigemReserva origem, Long origemId) {
        return todas.stream()
                .filter(reserva -> reserva.getOrigem() == origem && reserva.getOrigemId().equals(origemId))
                .toList();
    }

    @Override
    public List<Reserva> listarPorOrigemVencendoDepoisDe(
            OrigemReserva origem, Long origemId, LocalDateTime vencendoDepoisDe) {
        return todas.stream()
                .filter(reserva ->
                        reserva.getOrigem() == origem
                                && reserva.getOrigemId().equals(origemId)
                                && reserva.getExpiraEm().isAfter(vencendoDepoisDe))
                .toList();
    }

    @Override
    public Reserva salvar(Reserva reserva) {
        if (reserva.getId() != null) {
            return reserva;
        }
        Reserva salva =
                new Reserva(
                        proximoId++,
                        reserva.getLivroId(),
                        reserva.getOrigem(),
                        reserva.getOrigemId(),
                        reserva.getQuantidade(),
                        reserva.getExpiraEm());
        todas.add(salva);
        return salva;
    }

    @Override
    public void remover(Long reservaId) {
        todas.removeIf(reserva -> reserva.getId().equals(reservaId));
    }

    @Override
    public int quantidadeReservadaAtivaExceto(Long livroId, LocalDateTime agora, Long reservaExcluida) {
        return todas.stream()
                .filter(reserva ->
                        reserva.getLivroId().equals(livroId)
                                && reserva.ativaEm(agora)
                                && !reserva.getId().equals(reservaExcluida))
                .mapToInt(Reserva::getQuantidade)
                .sum();
    }
}
