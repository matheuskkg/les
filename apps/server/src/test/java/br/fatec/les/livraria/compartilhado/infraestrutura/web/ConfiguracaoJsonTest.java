package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

class ConfiguracaoJsonTest {

    record Exemplo(Dinheiro preco, LocalDate data, Instant momento, YearMonth validade, @Nullable String opcional) {
    }

    record ComPreco(Dinheiro preco) {
    }

    private final JsonMapper json = JsonMapper.builder()
            .addModule(new ConfiguracaoJson().moduloDaLivraria())
            .build();

    @Test
    void escreveDinheiroComoStringEDatasEmIso8601() {
        Exemplo exemplo = new Exemplo(Dinheiro.de("1234.5"), LocalDate.of(2026, 10, 21),
                Instant.parse("2026-10-21T14:30:00Z"), YearMonth.of(2027, 8), "x");

        assertThat(json.writeValueAsString(exemplo)).isEqualTo(
                "{\"preco\":\"1234.50\",\"data\":\"2026-10-21\",\"momento\":\"2026-10-21T14:30:00Z\","
                        + "\"validade\":\"2027-08\",\"opcional\":\"x\"}");
    }

    @Test
    void leDinheiroDeString() {
        assertThat(json.readValue("{\"preco\":\" 10.5 \"}", ComPreco.class).preco()).isEqualTo(Dinheiro.de("10.50"));
    }

    @Test
    void recusaDinheiroComoNumero() {
        assertThatThrownBy(() -> json.readValue("{\"preco\":10.5}", ComPreco.class))
                .isInstanceOf(DatabindException.class);
    }

    @Test
    void recusaDinheiroInvalido() {
        assertThatThrownBy(() -> json.readValue("{\"preco\":\"dez\"}", ComPreco.class))
                .isInstanceOf(DatabindException.class);
    }
}
