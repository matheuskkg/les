package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.OrdenacaoPermitida;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

/** Montagem da paginação a partir dos query params (DEC-07, RF-CLI-004, RF-LIV-005). */
class CriadorDePaginacaoTest {

    private static final OrdenacaoPermitida PERMITIDA = OrdenacaoPermitida
            .de(List.of(Ordem.de("nome,asc")), "nome", "email")
            .com("cadastro", "dataCadastro");

    private final CriadorDePaginacao criador = new CriadorDePaginacao();

    @Test
    void semParametrosUsaOsPadroesEAIgualOrdemPadraoDaListagem() {
        Paginacao paginacao = criador.criar(null, null, null, PERMITIDA);

        assertThat(paginacao.pagina()).isZero();
        assertThat(paginacao.tamanho()).isEqualTo(20);
        assertThat(paginacao.ordem()).containsExactly(new Ordem("nome", Ordem.Direcao.ASC));
    }

    @Test
    void traduzOsCamposInformadosParaAsPropriedadesDoModelo() {
        Paginacao paginacao = criador.criar(2, 50,
                List.of("cadastro,desc", "email,asc"), PERMITIDA);

        assertThat(paginacao.pagina()).isEqualTo(2);
        assertThat(paginacao.tamanho()).isEqualTo(50);
        assertThat(paginacao.ordem()).containsExactly(
                new Ordem("dataCadastro", Ordem.Direcao.DESC),
                new Ordem("email", Ordem.Direcao.ASC));
    }

    @Test
    void ignoraValoresEmBrancoDaOrdem() {
        Paginacao paginacao = criador.criar(null, null, List.of(" ", "email,desc"), PERMITIDA);

        assertThat(paginacao.ordem()).containsExactly(new Ordem("email", Ordem.Direcao.DESC));
    }

    @Test
    void recusaCampoNaoDeclarado() {
        assertThatThrownBy(() -> criador.criar(null, null, List.of("senha,asc"), PERMITIDA))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("senha")
                .hasMessageContaining("cadastro");
    }

    @Test
    void recusaOrdemMalformada() {
        assertThatThrownBy(() -> criador.criar(null, null, List.of("nome"), PERMITIDA))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("formato campo,asc|desc");
    }

    @Test
    void recusaPaginaETamanhoInvalidos() {
        assertThatThrownBy(() -> criador.criar(-1, null, null, PERMITIDA))
                .isInstanceOf(ParametroInvalido.class);
        assertThatThrownBy(() -> criador.criar(null, 0, null, PERMITIDA))
                .isInstanceOf(ParametroInvalido.class);
        assertThatThrownBy(() -> criador.criar(null, 101, null, PERMITIDA))
                .isInstanceOf(ParametroInvalido.class);
    }
}
