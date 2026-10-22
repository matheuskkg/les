package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;

/** Formatos do JSON da API (DEC-07). Datas usam o padrão ISO-8601 do Jackson. */
@Configuration
public class ConfiguracaoJson {

    @Bean
    JacksonModule moduloDaLivraria() {
        return new SimpleModule("livraria")
                .addSerializer(Dinheiro.class, new SerializadorDeDinheiro())
                .addDeserializer(Dinheiro.class, new DesserializadorDeDinheiro());
    }
}
