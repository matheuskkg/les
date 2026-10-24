package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.OrdenacaoPermitida;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

/** Configuração de {@link Listagem} validada como erro de programação (DEC-07). */
class OrdenacoesDeListagemTest {

    static class ControllerDeTeste {

        void valida(@Listagem(campos = { "nome", "cadastro=dataCadastro" }, padrao = "cadastro,desc")
                Paginacao paginacao) {
        }

        void padraoMalformado(@Listagem(campos = "nome", padrao = "nome") Paginacao paginacao) {
        }

        void padraoNaoDeclarado(@Listagem(campos = "nome", padrao = "email,asc") Paginacao paginacao) {
        }

        void campoMalformado(@Listagem(campos = "nome=") Paginacao paginacao) {
        }
    }

    private final OrdenacoesDeListagem ordenacoes = new OrdenacoesDeListagem();

    @Test
    void montaEValidaUmaVezPorMetodo() throws Exception {
        MethodParameter parametro = parametro("valida");

        OrdenacaoPermitida permitida = ordenacoes.de(parametro);

        assertThat(permitida.propriedade("cadastro")).isEqualTo("dataCadastro");
        assertThat(permitida.padrao()).containsExactly(new Ordem("cadastro", Ordem.Direcao.DESC));
        assertThat(ordenacoes.de(parametro)).isSameAs(permitida);
    }

    @Test
    void recusaPadraoMalformadoComoErroDeConfiguracao() throws Exception {
        assertThatThrownBy(() -> ordenacoes.de(parametro("padraoMalformado")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("padraoMalformado")
                .hasMessageContaining("formato campo,asc|desc")
                .isNotInstanceOf(ParametroInvalido.class);
    }

    @Test
    void recusaPadraoNaoDeclaradoComoErroDeConfiguracao() throws Exception {
        assertThatThrownBy(() -> ordenacoes.de(parametro("padraoNaoDeclarado")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("padraoNaoDeclarado")
                .hasMessageContaining("email");
    }

    @Test
    void recusaCampoMalformadoComoErroDeConfiguracao() throws Exception {
        assertThatThrownBy(() -> ordenacoes.de(parametro("campoMalformado")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("campoMalformado")
                .hasMessageContaining("nome=");
    }

    private static MethodParameter parametro(String nome) throws Exception {
        Method metodo = ControllerDeTeste.class.getDeclaredMethod(nome, Paginacao.class);
        return new MethodParameter(metodo, 0);
    }
}
