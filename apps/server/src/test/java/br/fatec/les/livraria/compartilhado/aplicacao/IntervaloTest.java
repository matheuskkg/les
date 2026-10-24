package br.fatec.les.livraria.compartilhado.aplicacao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Validação dos intervalos De/Ate das consultas (DEC-07). */
class IntervaloTest {

    @Test
    void aceitaIntervaloValidoEOsLimitesNulos() {
        assertThatCode(() -> Intervalo.validar("valor", new BigDecimal("10"), new BigDecimal("10")))
                .doesNotThrowAnyException();
        assertThatCode(() -> Intervalo.validar("valor", null, new BigDecimal("10")))
                .doesNotThrowAnyException();
        assertThatCode(() -> Intervalo.validar("data", LocalDate.of(2026, 1, 1), null))
                .doesNotThrowAnyException();
    }

    @Test
    void recusaInicioMaiorQueOFim() {
        assertThatThrownBy(() -> Intervalo.validar("valor", new BigDecimal("20"), new BigDecimal("10")))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("valor")
                .hasMessageContaining("maior");
    }
}
