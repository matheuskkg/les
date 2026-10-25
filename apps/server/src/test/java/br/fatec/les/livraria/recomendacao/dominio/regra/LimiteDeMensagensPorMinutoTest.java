package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.EnvioDeMensagem;

class LimiteDeMensagensPorMinutoTest {

    private final LimiteDeMensagensPorMinuto regra = new LimiteDeMensagensPorMinuto();

    private Violacoes validar(long enviadas) {
        Violacoes violacoes = new Violacoes();
        regra.validar(new EnvioDeMensagem("Oi", enviadas), violacoes);
        return violacoes;
    }

    @Test
    void rnfRec001_aceitaADecimaMensagemDoMinuto() {
        assertThat(validar(9).vazia()).isTrue();
    }

    @Test
    void rnfRec001_recusaAPartirDaDecimaPrimeira() {
        assertThat(validar(10).lista()).extracting(Violacao::codigo).containsExactly("RNF-REC-001");
    }
}
