package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Especificação OpenAPI da API, fonte dos tipos do {@code web} (DEC-07). Fica em {@code /v3/api-docs} e é versionada
 * em {@code apps/web/openapi.json}.
 */
@Configuration
public class ConfiguracaoOpenApi {

    @Bean
    OpenAPI especificacao() {
        return new OpenAPI()
                .info(new Info().title("API da Livraria").version("1"))
                .servers(List.of(new Server().url("/")));
    }

    @Bean
    ModelConverter conversorDeEsquemas() {
        return new ConversorDeEsquemas();
    }

    @Bean
    OperationCustomizer parametrosDeListagem() {
        return new ParametrosDeListagemOpenApi();
    }
}
