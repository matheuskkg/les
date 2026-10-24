package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.CanaisDeEventos;

/** Encerramento dos canais SSE no logout (DEC-13). */
class EncerrarCanaisTest {

    @Test
    void dec13_encerraOsCanaisDoUsuario() {
        CanaisDeEventosFake canais = new CanaisDeEventosFake();

        new EncerrarCanais(canais).executar(42L);

        assertThat(canais.encerrados()).containsExactly(42L);
    }

    private static final class CanaisDeEventosFake implements CanaisDeEventos {

        private final List<Long> encerrados = new ArrayList<>();

        @Override
        public void encerrar(Long usuarioId) {
            encerrados.add(usuarioId);
        }

        List<Long> encerrados() {
            return List.copyOf(encerrados);
        }
    }
}
