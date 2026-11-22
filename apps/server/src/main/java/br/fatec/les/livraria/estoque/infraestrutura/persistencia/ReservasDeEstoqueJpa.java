package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Adapter JPA das reservas de estoque (DEC-15.3, DEC-16). */
@Component
@RequiredArgsConstructor
public class ReservasDeEstoqueJpa implements ReservasDeEstoque {

    private final ReservaJpaRepository repositorio;
    private final ReservaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> buscar(Long livroId, OrigemReserva origem, Long origemId) {
        return repositorio.findByLivroIdAndOrigemAndOrigemId(livroId, origem, origemId).map(mapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPorOrigem(OrigemReserva origem, Long origemId) {
        return repositorio.findByOrigemAndOrigemId(origem, origemId).stream().map(mapper::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPorOrigemVencendoDepoisDe(
            OrigemReserva origem, Long origemId, LocalDateTime vencendoDepoisDe) {
        return repositorio.findByOrigemAndOrigemIdAndExpiraEmAfter(origem, origemId, vencendoDepoisDe).stream()
                .map(mapper::paraDominio)
                .toList();
    }

    @Override
    @Transactional
    public Reserva salvar(Reserva reserva) {
        return mapper.paraDominio(repositorio.save(mapper.paraJpa(reserva)));
    }

    @Override
    @Transactional
    public void remover(Long reservaId) {
        repositorio.deleteById(reservaId);
    }

    @Override
    @Transactional(readOnly = true)
    public int quantidadeReservadaAtivaExceto(Long livroId, LocalDateTime agora, Long reservaExcluida) {
        return repositorio.somarReservasAtivasExceto(livroId, agora, reservaExcluida);
    }
}
