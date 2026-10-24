package br.fatec.les.livraria.vendas.dominio.troca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

class TrocaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Test
    void rfVen012_autorizaTrocaEmTroca() {
        Troca troca = emTroca();

        troca.autorizar();

        assertThat(troca.getStatus()).isEqualTo(StatusTroca.TROCA_AUTORIZADA);
    }

    @Test
    void dec12_recusaGuardaOMotivoSemEspacosNasPontas() {
        Troca troca = emTroca();

        troca.recusar("  Livro com marcas de uso  ");

        assertThat(troca.getStatus()).isEqualTo(StatusTroca.TROCA_RECUSADA);
        assertThat(troca.getMotivoRecusa()).isEqualTo("Livro com marcas de uso");
        assertThat(troca.recusada()).isTrue();
    }

    @Test
    void dec12_soTrocaEmTrocaPodeSerAutorizadaOuRecusada() {
        for (StatusTroca status : List.of(StatusTroca.TROCA_AUTORIZADA, StatusTroca.TROCA_RECUSADA,
                StatusTroca.TROCADO)) {
            Troca troca = new Troca(1L, 1L, status, AGORA, List.of(new ItemTroca(1L, 101L, 1, null)), null, null,
                    null);

            assertThatThrownBy(troca::autorizar).isInstanceOf(ExcecaoDeConflito.class);
            assertThatThrownBy(() -> troca.recusar("Motivo")).isInstanceOf(ExcecaoDeConflito.class);
            assertThat(troca.getStatus()).isEqualTo(status);
        }
    }

    private static Troca emTroca() {
        return new Troca(1L, 1L, StatusTroca.EM_TROCA, AGORA, List.of(new ItemTroca(1L, 101L, 1, null)), null, null,
                null);
    }
}
