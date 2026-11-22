package br.fatec.les.livraria.acesso.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class SituacaoDaContaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 23, 12, 0);

    private final SituacaoDaConta situacaoDaConta = new SituacaoDaConta();

    @Test
    void contaSemInativacaoEstaAtiva() {
        assertThat(situacaoDaConta.calcular(usuario(null), AGORA)).isEqualTo(SituacaoConta.ATIVA);
    }

    @Test
    void contaInativadaHaMenosDeSeteDiasEstaInativa_RF_CLI_003() {
        assertThat(situacaoDaConta.calcular(usuario(AGORA.minusDays(6)), AGORA)).isEqualTo(SituacaoConta.INATIVA);
    }

    @Test
    void contaInativadaHaExatamenteSeteDiasAindaEstaInativa_RF_CLI_003() {
        assertThat(situacaoDaConta.calcular(usuario(AGORA.minusDays(7)), AGORA)).isEqualTo(SituacaoConta.INATIVA);
    }

    @Test
    void contaInativadaHaMaisDeSeteDiasEstaEncerrada_RF_CLI_003() {
        assertThat(situacaoDaConta.calcular(usuario(AGORA.minusDays(7).minusSeconds(1)), AGORA))
                .isEqualTo(SituacaoConta.ENCERRADA);
    }

    private static Usuario usuario(LocalDateTime inativadoEm) {
        return new Usuario(1L, "ana@teste.test", null, "hash", Papel.CLIENTE, inativadoEm);
    }
}
