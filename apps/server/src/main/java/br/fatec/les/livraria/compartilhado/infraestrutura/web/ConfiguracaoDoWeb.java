package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serve o build do {@code web} copiado para {@code static/} (DEC-09). As rotas do {@code web} são do lado do cliente:
 * um caminho fora de {@code /api} e {@code /v3} que não seja arquivo devolve o {@code index.html}.
 */
@Configuration
public class ConfiguracaoDoWeb implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registro) {
        registro.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new ResolvedorDoWeb());
    }

    static class ResolvedorDoWeb extends PathResourceResolver {

        @Override
        protected Resource getResource(String caminho, Resource local) throws IOException {
            Resource arquivo = super.getResource(caminho, local);
            if (arquivo != null || !ehRotaDoWeb(caminho)) {
                return arquivo;
            }
            return super.getResource("index.html", local);
        }

        private static boolean ehRotaDoWeb(String caminho) {
            boolean daApi = caminho.equals("api") || caminho.startsWith("api/")
                    || caminho.equals("v3") || caminho.startsWith("v3/");
            boolean ehArquivo = caminho.substring(caminho.lastIndexOf('/') + 1).contains(".");
            return !daApi && !ehArquivo;
        }
    }
}
