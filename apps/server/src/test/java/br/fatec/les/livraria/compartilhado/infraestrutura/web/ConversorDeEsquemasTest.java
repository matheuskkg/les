package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

class ConversorDeEsquemasTest {

    record Item(String titulo, Dinheiro preco) {}

    record Cartao(String numero, YearMonth validade, @Nullable String apelido, Item item) {}

    private final Map<String, Schema<?>> esquemas = esquemasDe(Cartao.class);

    private static Map<String, Schema<?>> esquemasDe(Class<?> classe) {
        ModelConverters conversores = new ModelConverters();
        conversores.addConverter(new ConversorDeEsquemas());
        Map<String, Schema<?>> esquemas = new HashMap<>();
        conversores.readAll(classe).forEach(esquemas::put);
        return esquemas;
    }

    @Test
    void dinheiroEValidadeSaoStrings() {
        assertThat(propriedade("Item", "preco").getType()).isEqualTo("string");
        assertThat(propriedade("Cartao", "validade").getType()).isEqualTo("string");
    }

    private Schema<?> propriedade(String esquema, String nome) {
        return esquemas.get(esquema).getProperties().get(nome);
    }

    @Test
    void componentesDoRecordSaoObrigatoriosExcetoNullable() {
        assertThat(esquemas.get("Cartao").getRequired()).containsExactlyInAnyOrder("numero", "validade", "item");
        assertThat(esquemas.get("Item").getRequired()).containsExactlyInAnyOrder("titulo", "preco");
    }
}
