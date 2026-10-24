package br.fatec.les.livraria.vendas.infraestrutura.agendamento;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ExpirarReservas;
import lombok.RequiredArgsConstructor;

/** Executa a tarefa {@link ExpirarReservas} a cada 30 segundos (DEC-13). */
@Component
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
