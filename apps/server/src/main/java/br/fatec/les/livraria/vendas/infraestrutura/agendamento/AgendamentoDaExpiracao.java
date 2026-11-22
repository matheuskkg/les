package br.fatec.les.livraria.vendas.infraestrutura.agendamento;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ExpirarReservas;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Executa a tarefa {@link ExpirarReservas} a cada 30 segundos (DEC-13). Desligado por {@code
 * livraria.expiracao.agendada=false} nos testes e no profile {@code e2e}, que executam a tarefa com o relógio
 * controlado (DEC-09).
 */
@Component
@ConditionalOnProperty(name = "livraria.expiracao.agendada", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AgendamentoDaExpiracao {

    /** Intervalo da tarefa definido no DEC-13. */
    static final long INTERVALO_EM_MILISSEGUNDOS = 30_000L;

    private final ExpirarReservas expirarReservas;

    @Scheduled(fixedRate = INTERVALO_EM_MILISSEGUNDOS)
    public void executar() {
        expirarReservas.executar();
    }
}
