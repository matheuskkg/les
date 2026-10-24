package br.fatec.les.livraria.notificacoes.infraestrutura.sse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Comentário de heartbeat a cada 25 segundos nos canais SSE abertos (DEC-13). */
@Component
@RequiredArgsConstructor
public class BatimentoDosCanais {

    /** Intervalo do heartbeat definido no DEC-13. */
    static final long INTERVALO_EM_MILISSEGUNDOS = 25_000L;

    private final CanaisSse canais;

    @Scheduled(fixedRate = INTERVALO_EM_MILISSEGUNDOS)
    public void baterPonto() {
        canais.baterPonto();
    }
}
