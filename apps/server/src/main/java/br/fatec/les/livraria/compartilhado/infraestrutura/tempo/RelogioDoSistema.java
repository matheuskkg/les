package br.fatec.les.livraria.compartilhado.infraestrutura.tempo;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class RelogioDoSistema implements Relogio {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final Clock clock = Clock.system(FUSO);

    @Override
    public LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
