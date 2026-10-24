package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Resolve um parâmetro {@link Paginacao} anotado com {@link Listagem} a partir dos query params {@code pagina},
 * {@code tamanho} e {@code ordem} (DEC-07, RNF-GER-001).
 *
 * <p>Os valores de {@code ordem} são lidos crus, sem a conversão do Spring que separa listas por vírgula, porque a
 * própria ordenação usa vírgula entre campo e direção.
 */
@Component
@RequiredArgsConstructor
public class ResolvedorDePaginacao implements HandlerMethodArgumentResolver {

    private final CriadorDePaginacao criador;
    private final OrdenacoesDeListagem ordenacoes;

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.getParameterType() == Paginacao.class
                && parametro.hasParameterAnnotation(Listagem.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer mav,
            NativeWebRequest requisicao, WebDataBinderFactory binderFactory) {
        HttpServletRequest http = requisicao.getNativeRequest(HttpServletRequest.class);
        if (http == null) {
            throw new IllegalStateException("Parâmetro de listagem sem a requisição HTTP.");
        }
        Integer pagina = inteiro(http.getParameter("pagina"), "pagina");
        Integer tamanho = inteiro(http.getParameter("tamanho"), "tamanho");
        String[] valores = http.getParameterValues("ordem");
        List<String> ordem = valores == null ? List.of() : List.of(valores);
        return criador.criar(pagina, tamanho, ordem, ordenacoes.de(parametro));
    }

    private static Integer inteiro(String valor, String parametro) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException excecao) {
            throw new ParametroInvalido(parametro, "O parâmetro " + parametro + " deve ser um número inteiro.");
        }
    }
}
