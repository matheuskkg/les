package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Regressão do bypass de autorização por caminho codificado (INF-06): as regras do Spring Security precisam olhar o
 * caminho decodificado. O {@link HttpClient} envia a URI crua, sem re-codificar {@code %61}, e o MockMvc não reproduz
 * a decodificação do Tomcat.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SegurancaCaminhosCodificadosTest {

    @RestController
    static class ControllerDeAdmin {

        private final AtomicInteger acessos = new AtomicInteger();

        @GetMapping("/api/admin/exemplo")
        String admin() {
            acessos.incrementAndGet();
            return "admin";
        }

        @GetMapping("/api/me/exemplo")
        String meu() {
            acessos.incrementAndGet();
            return "me";
        }
    }

    @TestConfiguration
    static class Configuracao {

        @Bean
        ControllerDeAdmin controllerDeAdmin() {
            return new ControllerDeAdmin();
        }
    }

    @LocalServerPort
    private int porta;

    @Autowired
    private ControllerDeAdmin controller;

    private final HttpClient cliente = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @ParameterizedTest
    @ValueSource(strings = {
            "/%61pi/admin/exemplo",
            "/%61pi/me/exemplo",
            "/api/%61dmin/exemplo",
            "/api//admin/exemplo",
            "/API/admin/exemplo",
            "/api/admin/../admin/exemplo"
    })
    void caminhoAlternativoNaoAlcancaOControllerProtegido(String caminho) throws Exception {
        controller.acessos.set(0);

        HttpResponse<String> resposta = cliente.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(controller.acessos).hasValue(0);
        assertThat(resposta.statusCode())
                .as("GET %s não pode chegar ao controller de admin (resposta: %s)", caminho, resposta.body())
                .isIn(400, 401);
    }
}
