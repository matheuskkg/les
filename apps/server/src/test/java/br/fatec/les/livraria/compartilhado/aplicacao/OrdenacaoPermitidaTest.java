package br.fatec.les.livraria.compartilhado.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.List;

/** Declaração dos campos ordenáveis de cada listagem (DEC-07). */
class OrdenacaoPermitidaTest {

    @Test
    void traduzOCampoDaApiParaAPropriedadeDoModelo() {
        OrdenacaoPermitida permitida =
                OrdenacaoPermitida.de(List.of(Ordem.de("nome,asc")), "nome", "email").com("cadastro", "dataCadastro");

        assertThat(permitida.propriedade("cadastro")).isEqualTo("dataCadastro");
        assertThat(permitida.propriedade("nome")).isEqualTo("nome");
        assertThat(permitida.padrao()).containsExactly(new Ordem("nome", Ordem.Direcao.ASC));
    }

    @Test
    void recusaCampoNaoDeclarado() {
        OrdenacaoPermitida permitida = OrdenacaoPermitida.de(List.of(), "nome");

        assertThatThrownBy(() -> permitida.propriedade("senha"))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("senha")
                .hasMessageContaining("nome");
    }

    @Test
    void recusaOrdemPadraoNaoDeclarada() {
        assertThatThrownBy(() -> OrdenacaoPermitida.de(List.of(Ordem.de("nome,asc")), "email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");
    }
}
