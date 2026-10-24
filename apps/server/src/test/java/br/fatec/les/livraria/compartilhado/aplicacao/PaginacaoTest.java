package br.fatec.les.livraria.compartilhado.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Validação da entrada de paginação (DEC-07, RNF-GER-001). */
class PaginacaoTest {

    @Test
    void usaOsValoresPadrao() {
        Paginacao paginacao = Paginacao.padrao();

        assertThat(paginacao.pagina()).isZero();
        assertThat(paginacao.tamanho()).isEqualTo(20);
        assertThat(paginacao.ordem()).isEmpty();
    }

    @Test
    void aceitaOsLimitesDePaginaETamanho() {
        assertThat(new Paginacao(0, 1, List.of()).tamanho()).isEqualTo(1);
        assertThat(new Paginacao(10, 100, List.of()).pagina()).isEqualTo(10);
    }

    @Test
    void recusaPaginaNegativa() {
        assertThatThrownBy(() -> new Paginacao(-1, 20, List.of()))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("página")
                .satisfies(e -> assertThat(((ParametroInvalido) e).getParametro()).isEqualTo("pagina"));
    }

    @Test
    void recusaTamanhoMenorQueUmEMaiorQueCem() {
        assertThatThrownBy(() -> new Paginacao(0, 0, List.of()))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("entre 1 e 100");
        assertThatThrownBy(() -> new Paginacao(0, 101, List.of()))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("entre 1 e 100");
    }

    @Test
    void copiaAOrdenacaoParaNaoDependerDaListaDoChamador() {
        List<Ordem> ordem = new ArrayList<>(List.of(Ordem.de("nome,asc")));
        Paginacao paginacao = new Paginacao(0, 20, ordem);

        ordem.clear();

        assertThat(paginacao.ordem()).containsExactly(new Ordem("nome", Ordem.Direcao.ASC));
        assertThatThrownBy(() -> paginacao.ordem().add(Ordem.de("id,asc")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
