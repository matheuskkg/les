package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.aplicacao.OrdenacaoPermitida;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monta e valida a {@link OrdenacaoPermitida} de cada método de listagem uma única vez (DEC-07). A configuração é de
 * programação: erros viram {@link IllegalStateException} com a indicação do método, nunca HTTP 400.
 */
@Component
class OrdenacoesDeListagem {

    private final Map<Method, OrdenacaoPermitida> cache = new ConcurrentHashMap<>();

    OrdenacaoPermitida de(MethodParameter parametro) {
        Listagem listagem = parametro.getParameterAnnotation(Listagem.class);
        Method metodo = parametro.getMethod();
        if (listagem == null || metodo == null) {
            throw new IllegalStateException("Parâmetro de listagem sem @Listagem ou sem método associado.");
        }
        return de(metodo, listagem);
    }

    OrdenacaoPermitida de(Method metodo, Listagem listagem) {
        return cache.computeIfAbsent(metodo, chave -> montar(chave, listagem));
    }

    private static OrdenacaoPermitida montar(Method metodo, Listagem listagem) {
        try {
            return CamposDeListagem.ordenacao(listagem);
        } catch (ParametroInvalido | IllegalArgumentException excecao) {
            throw new IllegalStateException(
                    "Configuração inválida em @Listagem do método "
                            + metodo.toGenericString()
                            + ": "
                            + excecao.getMessage()
                            + " Corrija a anotação; este é um erro de programação, não um parâmetro da requisição.",
                    excecao);
        }
    }
}
