package br.fatec.les.livraria.compartilhado.infraestrutura.e2e;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/** Relógio que os testes E2E podem fixar; sem data fixada, segue o do sistema (DEC-09). */
@Primary
@Component
@Profile("e2e")
@RequiredArgsConstructor
public class RelogioAjustavel implements Relogio {

    private final RelogioDoSistema sistema;
    private final AtomicReference<LocalDateTime> fixado = new AtomicReference<>();

    @Override
    public LocalDateTime agora() {
        LocalDateTime agora = fixado.get();
        return agora != null ? agora : sistema.agora();
    }

    public void fixar(Instant instante) {
        fixado.set(LocalDateTime.ofInstant(instante, RelogioDoSistema.FUSO));
    }

    public void voltarAoSistema() {
        fixado.set(null);
    }
}
