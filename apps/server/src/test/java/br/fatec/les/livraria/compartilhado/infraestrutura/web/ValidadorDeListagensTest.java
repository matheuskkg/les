package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

/** Validação das anotações {@link Listagem} na inicialização (DEC-07). */
class ValidadorDeListagensTest {

    static class ControllerDeTeste {

        void valida(
                @Listagem(
                                campos = {"nome", "cadastro=dataCadastro"},
                                padrao = "cadastro,desc")
                        Paginacao paginacao) {}

        void padraoNaoDeclarado(@Listagem(campos = "nome", padrao = "email,asc") Paginacao paginacao) {}

        void tipoErrado(@Listagem(campos = "nome") String valor) {}
    }

    @Test
    void aceitaConfiguracaoValida() throws Exception {
        assertThatCode(() -> validador("valida", Paginacao.class).afterSingletonsInstantiated())
                .doesNotThrowAnyException();
    }

    @Test
    void falhaNaInicializacaoComPadraoNaoDeclarado() throws Exception {
        assertThatThrownBy(() -> validador("padraoNaoDeclarado", Paginacao.class).afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("padraoNaoDeclarado")
                .hasMessageContaining("Configuração inválida");
    }

    @Test
    void falhaNaInicializacaoQuandoOParametroNaoEhPaginacao() throws Exception {
        assertThatThrownBy(() -> validador("tipoErrado", String.class).afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tipoErrado")
                .hasMessageContaining(Paginacao.class.getName());
    }

    private static ValidadorDeListagens validador(String nome, Class<?> tipo) throws Exception {
        Method metodo = ControllerDeTeste.class.getDeclaredMethod(nome, tipo);
        HandlerMethod manipulador = new HandlerMethod(new ControllerDeTeste(), metodo);
        RequestMappingHandlerMapping mapeamento =
                new RequestMappingHandlerMapping() {
                    @Override
                    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
                        return Map.of(RequestMappingInfo.paths("/teste").build(), manipulador);
                    }
                };
        return new ValidadorDeListagens(mapeamento, new OrdenacoesDeListagem());
    }
}
