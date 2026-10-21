package br.fatec.les.livraria.compartilhado.infraestrutura.tempo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

@Component
public class RelogioDoSistema implements Relogio {

    private final Clock clock = Clock.system(ZoneId.of("America/Sao_Paulo"));

    @Override
    public LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
