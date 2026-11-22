package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.RecordComponent;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Ajusta os esquemas do OpenAPI ao formato real do JSON (DEC-07).
 *
 * <ul>
 *   <li>{@link Dinheiro} e {@link YearMonth} são strings.
 *   <li>Os componentes de um record são obrigatórios, exceto os anotados com {@link Nullable}.
 * </ul>
 */
class ConversorDeEsquemas implements ModelConverter {

    private static final String PREFIXO_REF = "#/components/schemas/";

    @Override
    public Schema<?> resolve(AnnotatedType tipo, ModelConverterContext contexto, Iterator<ModelConverter> cadeia) {
        Class<?> classe = Json.mapper().constructType(tipo.getType()).getRawClass();
        if (classe == Dinheiro.class) {
            return new StringSchema().format("decimal").example("1234.56");
        }
        if (classe == YearMonth.class) {
            return new StringSchema().pattern("^\\d{4}-\\d{2}$").example("2027-08");
        }
        if (!cadeia.hasNext()) {
            return null;
        }
        Schema<?> esquema = cadeia.next().resolve(tipo, contexto, cadeia);
        if (esquema != null && classe.isRecord()) {
            marcarObrigatorios(definicao(esquema, contexto), classe);
        }
        return esquema;
    }

    private static Schema<?> definicao(Schema<?> esquema, ModelConverterContext contexto) {
        String ref = esquema.get$ref();
        if (ref == null || !ref.startsWith(PREFIXO_REF)) {
            return esquema;
        }
        return contexto.getDefinedModels().get(ref.substring(PREFIXO_REF.length()));
    }

    private static void marcarObrigatorios(Schema<?> esquema, Class<?> record) {
        if (esquema == null) {
            return;
        }
        if (esquema.getProperties() == null) {
            return;
        }
        Set<String> propriedades = esquema.getProperties().keySet();
        List<String> obrigatorios = new ArrayList<>();
        for (RecordComponent componente : record.getRecordComponents()) {
            if (propriedades.contains(componente.getName()) && !anuladoPermitido(componente)) {
                obrigatorios.add(componente.getName());
            }
        }
        esquema.setRequired(obrigatorios.isEmpty() ? null : obrigatorios);
    }

    private static boolean anuladoPermitido(RecordComponent componente) {
        return componente.isAnnotationPresent(Nullable.class)
                || componente.getAnnotatedType().isAnnotationPresent(Nullable.class);
    }
}
