package br.fatec.les.livraria.compartilhado.infraestrutura.tempo;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Expõe o {@link Relogio} da aplicação como {@link Clock}, para que a validação de datas do JWT use a mesma fonte de
 * tempo controlável dos testes E2E (DEC-09).
 */
public final class RelogioComoClock extends Clock {

    private final Relogio relogio;
    private final ZoneId fuso;

    public RelogioComoClock(Relogio relogio) {
        this(relogio, RelogioDoSistema.FUSO);
    }

    private RelogioComoClock(Relogio relogio, ZoneId fuso) {
        this.relogio = relogio;
        this.fuso = fuso;
    }

    /** Instante atual do relógio da aplicação, no fuso do projeto. */
    public static Instant instante(Relogio relogio) {
        return instanteDe(relogio.agora(), RelogioDoSistema.FUSO);
    }

    private static Instant instanteDe(LocalDateTime agora, ZoneId fuso) {
        return agora.atZone(fuso).toInstant();
    }

    @Override
    public Instant instant() {
        return instanteDe(relogio.agora(), fuso);
    }

    @Override
    public ZoneId getZone() {
        return fuso;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return new RelogioComoClock(relogio, zona);
    }
}
