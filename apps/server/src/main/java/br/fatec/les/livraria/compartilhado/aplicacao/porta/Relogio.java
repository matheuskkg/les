package br.fatec.les.livraria.compartilhado.aplicacao.porta;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fonte da data/hora atual, substituível nos testes. */
public interface Relogio {

    LocalDateTime agora();

    default LocalDate hoje() {
        return agora().toLocalDate();
    }
}
