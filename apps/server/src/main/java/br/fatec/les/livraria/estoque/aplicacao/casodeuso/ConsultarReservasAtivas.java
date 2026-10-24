package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.porta.ReservasDeEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;
import lombok.RequiredArgsConstructor;

/**
 * Unidades ainda reservadas para um dono, por livro, para a finalização revalidar o estoque (RN-VEN-002, DEC-12).
 * Reserva vencida não conta: as unidades dela já podem ter ido para outro cliente.
 */
@Component
@RequiredArgsConstructor
public class ConsultarReservasAtivas {

    private final ReservasDeEstoque reservas;
    private final Relogio relogio;

    /** @return quantidade reservada por livro; livro sem reserva ativa fica de fora */
    @Transactional(readOnly = true)
    public Map<Long, Integer> executar(OrigemReserva origem, Long origemId) {
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(origemId, "origemId");
        LocalDateTime agora = relogio.agora();
        return reservas.listarPorOrigem(origem, origemId).stream()
                .filter(reserva -> reserva.ativaEm(agora))
                .collect(Collectors.toMap(Reserva::getLivroId, Reserva::getQuantidade, Integer::sum));
    }
}
