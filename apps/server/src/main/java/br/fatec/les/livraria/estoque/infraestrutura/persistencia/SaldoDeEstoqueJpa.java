package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import lombok.RequiredArgsConstructor;

/** Adapter JPA da porta {@link SaldoDeEstoque} (DEC-15.3). */
@Component
@RequiredArgsConstructor
public class SaldoDeEstoqueJpa implements SaldoDeEstoque {

    private final EstoqueJpaRepository estoques;
    private final ReservaJpaRepository reservas;

    @Override
    public Map<Long, Integer> quantidadesEmEstoque(Collection<Long> livroIds) {
        return estoques.buscarQuantidades(livroIds).stream()
                .collect(Collectors.toMap(
                        QuantidadeDoLivroProjecao::getLivroId,
                        QuantidadeDoLivroProjecao::getQuantidade));
    }

    @Override
    public Map<Long, Integer> reservasAtivas(Collection<Long> livroIds, LocalDateTime agora) {
        return reservas.somarReservasAtivas(livroIds, agora).stream()
                .collect(Collectors.toMap(
                        ReservaAtivaDoLivroProjecao::getLivroId,
                        reserva -> Math.toIntExact(reserva.getQuantidade())));
    }
}
