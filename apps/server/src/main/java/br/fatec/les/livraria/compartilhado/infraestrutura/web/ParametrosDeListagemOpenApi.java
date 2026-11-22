package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Declara no OpenAPI os query params das listagens resolvidas por {@link ResolvedorDePaginacao}, que o springdoc não
 * enxerga sozinho (DEC-07). A leitura de {@link Listagem} é a mesma do MVC ({@link CamposDeListagem}).
 */
class ParametrosDeListagemOpenApi implements OperationCustomizer {

    @Override
    public Operation customize(Operation operacao, HandlerMethod metodo) {
        for (MethodParameter parametro : metodo.getMethodParameters()) {
            Listagem listagem = parametro.getParameterAnnotation(Listagem.class);
            if (listagem == null || parametro.getParameterType() != Paginacao.class) {
                continue;
            }
            List<Parameter> parametros =
                    operacao.getParameters() == null ? new ArrayList<>() : new ArrayList<>(operacao.getParameters());
            adicionar(parametros, pagina());
            adicionar(parametros, tamanho());
            adicionar(parametros, ordem(listagem));
            operacao.setParameters(parametros);
        }
        return operacao;
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
        esquema.setMinimum(BigDecimal.valueOf(Paginacao.PAGINA_PADRAO));
        esquema.setDefault(Paginacao.PAGINA_PADRAO);
        return new Parameter()
                .in("query")
                .name("pagina")
                .description("Página, a partir de " + Paginacao.PAGINA_PADRAO + ".")
                .schema(esquema);
    }

    private static Parameter tamanho() {
        IntegerSchema esquema = new IntegerSchema();
        esquema.setFormat("int32");
        esquema.setMinimum(BigDecimal.ONE);
        esquema.setMaximum(BigDecimal.valueOf(Paginacao.TAMANHO_MAXIMO));
        esquema.setDefault(Paginacao.TAMANHO_PADRAO);
        return new Parameter()
                .in("query")
                .name("tamanho")
                .description(
                        "Itens por página (padrão "
                                + Paginacao.TAMANHO_PADRAO
                                + ", máximo "
                                + Paginacao.TAMANHO_MAXIMO
                                + ").")
                .schema(esquema);
    }

    private static Parameter ordem(Listagem listagem) {
        String descricao =
                "Ordenação no formato campo,asc|desc; pode ser repetido. Campos permitidos: "
                        + String.join(", ", CamposDeListagem.nomesDaApi(listagem))
                        + ".";
        descricao +=
                listagem.padrao().length == 0
                        ? " Sem ordem padrão."
                        : " Padrão: " + String.join(", ", listagem.padrao()) + ".";
        ArraySchema esquema = new ArraySchema();
        esquema.setItems(new StringSchema());
        Parameter parametro = new Parameter().in("query").name("ordem").description(descricao).schema(esquema);
        parametro.setStyle(Parameter.StyleEnum.FORM);
        parametro.setExplode(true);
        return parametro;
    }
}
