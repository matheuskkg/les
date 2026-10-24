package br.fatec.les.livraria.compartilhado.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Leitura do parâmetro repetível {@code ordem=campo,asc|desc} (DEC-07). */
class OrdemTest {

    @Test
    void leCampoEDirecao() {
        assertThat(Ordem.de("nome,asc")).isEqualTo(new Ordem("nome", Ordem.Direcao.ASC));
        assertThat(Ordem.de("precoVenda,desc")).isEqualTo(new Ordem("precoVenda", Ordem.Direcao.DESC));
    }

    @Test
    void ignoraEspacosEAceitaDirecaoEmQualquerCaixa() {
        assertThat(Ordem.de("  nome , DESC ")).isEqualTo(new Ordem("nome", Ordem.Direcao.DESC));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "nome", "nome,", ",asc", "nome,asc,desc" })
    void recusaFormatoInvalido(String valor) {
        assertThatThrownBy(() -> Ordem.de(valor))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("formato campo,asc|desc");
    }

    @Test
    void recusaDirecaoDesconhecida() {
        assertThatThrownBy(() -> Ordem.de("nome,maior"))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("asc ou desc");
    }

    @Test
    void recusaCampoEmBrancoNoConstrutor() {
        assertThatThrownBy(() -> new Ordem(" ", Ordem.Direcao.ASC))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("campo");
    }
}
