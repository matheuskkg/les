package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Declara no OpenAPI os query params das listagens resolvidas por {@link ResolvedorDePaginacao}, que o springdoc não
 * enxerga sozinho (DEC-07).
 */
class ParametrosDeListagemOpenApi implements OperationCustomizer {

    @Override
    public Operation customize(Operation operacao, HandlerMethod metodo) {
        for (MethodParameter parametro : metodo.getMethodParameters()) {
            Listagem listagem = parametro.getParameterAnnotation(Listagem.class);
            if (listagem == null || parametro.getParameterType() != Paginacao.class) {
                continue;
            }
            List<Parameter> parametros = operacao.getParameters() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(operacao.getParameters());
            removerParametroGerado(parametros);
            adicionar(parametros, pagina());
            adicionar(parametros, tamanho());
            adicionar(parametros, ordem(listagem));
            operacao.setParameters(parametros);
        }
        return operacao;
    }

    /** O springdoc documenta o próprio objeto {@code Paginacao}; a listagem usa os três params explícitos. */
    private static void removerParametroGerado(List<Parameter> parametros) {
        parametros.removeIf(ParametrosDeListagemOpenApi::apontaParaPaginacao);
    }

    private static boolean apontaParaPaginacao(Parameter parametro) {
        return parametro.getSchema() != null
                && parametro.getSchema().get$ref() != null
                && parametro.getSchema().get$ref().endsWith("/Paginacao");
    }

    private static void adicionar(List<Parameter> parametros, Parameter novo) {
        boolean existente = parametros.stream().anyMatch(p -> novo.getName().equals(p.getName()));
        if (!existente) {
            parametros.add(novo);
        }
    }

    private static Parameter pagina() {
        IntegerSchema esquema = new IntegerSchema();
        esquema.setFormat("int32");
        esquema.setExample(0);
        return new Parameter().in("query").name("pagina")
                .description("Página, a partir de 0 (padrão 0).")
                .schema(esquema);
    }

    private static Parameter tamanho() {
        IntegerSchema esquema = new IntegerSchema();
        esquema.setFormat("int32");
        esquema.setExample(20);
        return new Parameter().in("query").name("tamanho")
                .description("Itens por página (padrão 20, máximo 100).")
                .schema(esquema);
    }

    private static Parameter ordem(Listagem listagem) {
        String campos = Arrays.stream(listagem.campos())
                .map(campo -> campo.split("=", 2)[0].trim())
                .collect(Collectors.joining(", "));
        String descricao = "Ordenação no formato campo,asc|desc; pode ser repetido. Campos: " + campos + ".";
        if (listagem.padrao().length > 0) {
            descricao += " Padrão: " + String.join(", ", listagem.padrao()) + ".";
        }
        ArraySchema esquema = new ArraySchema();
        esquema.setItems(new StringSchema());
        esquema.setExample("nome,asc");
        Parameter parametro = new Parameter().in("query").name("ordem").description(descricao).schema(esquema);
        parametro.setStyle(Parameter.StyleEnum.FORM);
        parametro.setExplode(true);
        return parametro;
    }
}
