package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Valida todas as anotações {@link Listagem} na inicialização, para que um erro de configuração apareça no start da
 * aplicação e não como HTTP 400 na primeira requisição (DEC-07).
 */
@Component
@RequiredArgsConstructor
class ValidadorDeListagens implements SmartInitializingSingleton {

    private final RequestMappingHandlerMapping mapeamento;
    private final OrdenacoesDeListagem ordenacoes;

    @Override
    public void afterSingletonsInstantiated() {
        for (HandlerMethod manipulador : mapeamento.getHandlerMethods().values()) {
            validar(manipulador);
        }
    }

    private void validar(HandlerMethod manipulador) {
        for (MethodParameter parametro : manipulador.getMethodParameters()) {
            Listagem listagem = parametro.getParameterAnnotation(Listagem.class);
            if (listagem == null) {
                continue;
            }
            if (parametro.getParameterType() != Paginacao.class) {
                throw new IllegalStateException(
                        "Configuração inválida em @Listagem do método "
                                + manipulador.getMethod().toGenericString()
                                + ": o parâmetro anotado deve ser do tipo "
                                + Paginacao.class.getName()
                                + ".");
            }
            ordenacoes.de(parametro.getMethod(), listagem);
        }
    }
}
