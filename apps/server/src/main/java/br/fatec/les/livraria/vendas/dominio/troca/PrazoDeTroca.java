package br.fatec.les.livraria.vendas.dominio.troca;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

/** Prazo de troca (RN-VEN-013, DEC-12): até o fim do dia {@code entregueEm} + {@code livraria.troca.prazo}. */
public final class PrazoDeTroca {

    private PrazoDeTroca() {}

    /** Último dia em que a troca pode ser pedida, inteiro. */
    public static LocalDate ultimoDia(LocalDateTime entregueEm, Period prazo) {
        return Objects.requireNonNull(entregueEm, "entregueEm").toLocalDate().plus(Objects.requireNonNull(prazo));
    }

    /** Se {@code agora} ainda está dentro do último dia do prazo. */
    public static boolean dentro(LocalDateTime entregueEm, Period prazo, LocalDateTime agora) {
        return agora.isBefore(ultimoDia(entregueEm, prazo).plusDays(1).atStartOfDay());
    }
}
