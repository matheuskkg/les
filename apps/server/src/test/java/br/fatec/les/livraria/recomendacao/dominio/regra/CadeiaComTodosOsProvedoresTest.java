package br.fatec.les.livraria.recomendacao.dominio.regra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AlteracaoDaCadeia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PosicaoNaCadeia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class CadeiaComTodosOsProvedoresTest {

    private static final List<ProvedorIa> ATUAIS =
            List.of(
                    new ProvedorIa(1L, "gemini", 1, true),
                    new ProvedorIa(2L, "groq", 2, true),
                    new ProvedorIa(3L, "fake", 3, true));

    private final CadeiaComTodosOsProvedores regra = new CadeiaComTodosOsProvedores();

    private List<String> validar(PosicaoNaCadeia... posicoes) {
        Violacoes violacoes = new Violacoes();
        regra.validar(new AlteracaoDaCadeia(ATUAIS, Arrays.asList(posicoes)), violacoes);
        return violacoes.lista().stream().map(Violacao::mensagem).toList();
    }

    private static PosicaoNaCadeia ativo(Long id) {
        return new PosicaoNaCadeia(id, true);
    }

    @Test
    void rnfRec001_aceitaTodosOsProvedoresEmQualquerOrdem() {
        assertThat(validar(ativo(3L), ativo(1L), new PosicaoNaCadeia(2L, false))).isEmpty();
    }

    @Test
    void rnfRec001_aplicarGravaAOrdemPelaPosicaoEAAtivacao() {
        AlteracaoDaCadeia alteracao =
                new AlteracaoDaCadeia(ATUAIS, List.of(ativo(3L), new PosicaoNaCadeia(1L, false), ativo(2L)));

        assertThat(alteracao.aplicar())
                .extracting(ProvedorIa::id, ProvedorIa::nome, ProvedorIa::ordem, ProvedorIa::ativo)
                .containsExactly(tuple(3L, "fake", 1, true), tuple(1L, "gemini", 2, false), tuple(2L, "groq", 3, true));
    }

    @Test
    void rnfRec001_recusaProvedorDesconhecidoOuSemId() {
        assertThat(validar(ativo(1L), ativo(2L), ativo(3L), ativo(9L)))
                .containsExactly("A cadeia só pode ter os provedores cadastrados.");
        assertThat(validar(ativo(1L), ativo(2L), ativo(3L), ativo(null)))
                .containsExactly("A cadeia só pode ter os provedores cadastrados.");
    }

    @Test
    void rnfRec001_recusaProvedorRepetido() {
        assertThat(validar(ativo(1L), ativo(2L), ativo(3L), ativo(1L)))
                .containsExactly("Cada provedor aparece uma única vez na cadeia.");
    }

    @Test
    void rnfRec001_recusaProvedorFaltando() {
        assertThat(validar(ativo(1L), ativo(2L))).containsExactly("Informe todos os provedores da cadeia.");
        assertThat(validar()).containsExactly("Informe todos os provedores da cadeia.");
    }
}
