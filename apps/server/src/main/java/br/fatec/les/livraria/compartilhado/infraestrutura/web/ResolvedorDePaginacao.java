package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.OrdenacaoPermitida;
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

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.getParameterType() == Paginacao.class
                && parametro.hasParameterAnnotation(Listagem.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer mav,
            NativeWebRequest requisicao, WebDataBinderFactory binderFactory) {
        Listagem listagem = parametro.getParameterAnnotation(Listagem.class);
        HttpServletRequest http = requisicao.getNativeRequest(HttpServletRequest.class);
        if (listagem == null || http == null) {
            throw new IllegalStateException("Parâmetro de listagem sem a requisição HTTP.");
        }
        Integer pagina = inteiro(http.getParameter("pagina"), "pagina");
        Integer tamanho = inteiro(http.getParameter("tamanho"), "tamanho");
        String[] valores = http.getParameterValues("ordem");
        List<String> ordem = valores == null ? List.of() : List.of(valores);
        return criador.criar(pagina, tamanho, ordem, ordenacao(listagem));
    }

    private static OrdenacaoPermitida ordenacao(Listagem listagem) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (String campo : listagem.campos()) {
            String[] partes = campo.split("=", 2);
            String daApi = partes[0].trim();
            campos.put(daApi, partes.length == 2 ? partes[1].trim() : daApi);
        }
        List<Ordem> padrao = Arrays.stream(listagem.padrao()).map(Ordem::de).toList();
        return new OrdenacaoPermitida(campos, padrao);
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
