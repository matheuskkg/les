package br.fatec.les.livraria.compartilhado.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.List;

/** Envelope de saída das listagens (DEC-07). */
class PaginaTest {

    @Test
    void converteOsItensSemPerderOEnvelope() {
        Pagina<String> pagina = new Pagina<>(List.of("a", "b"), 1, 2, 5, 3);

        Pagina<Integer> convertida = pagina.map(String::length);

        assertThat(convertida.itens()).containsExactly(1, 1);
        assertThat(convertida.pagina()).isEqualTo(1);
        assertThat(convertida.tamanho()).isEqualTo(2);
        assertThat(convertida.totalItens()).isEqualTo(5);
        assertThat(convertida.totalPaginas()).isEqualTo(3);
    }

    @Test
    void protegeOsItensContraAlteracaoExterna() {
        Pagina<String> pagina = new Pagina<>(List.of("a"), 0, 20, 1, 1);

        assertThatThrownBy(() -> pagina.itens().add("b")).isInstanceOf(UnsupportedOperationException.class);
    }
}
