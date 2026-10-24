package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import br.fatec.les.livraria.acesso.infraestrutura.seguranca.ConversorDeJwt;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.ResolvedorDeTokenDaSessao;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security da API (DEC-08, DEC-15.4): JWT HS256 lido do cookie {@code sessao}, CSRF de SPA, rotas públicas e
 * papéis do banco. As sessões são stateless; não há formulário de login nem HTTP Basic.
 */
@Configuration
@RequiredArgsConstructor
public class ConfiguracaoDeSeguranca {

    private static final String H2_CONSOLE = "/h2-console/**";
    private static final String TESTES_E2E = "/api/teste/**";

    private final PropriedadesJwt propriedadesJwt;
    private final ResolvedorDeTokenDaSessao resolvedorDeToken;
    private final ConversorDeJwt conversorDeJwt;
    private final EntradaDeAutenticacao entradaDeAutenticacao;
    private final RespostaDeAcessoNegado respostaDeAcessoNegado;
    private final Environment ambiente;

    @Bean
    SecretKey chaveDoJwt() {
        return new SecretKeySpec(propriedadesJwt.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chave) {
        return NimbusJwtEncoder.withSecretKey(chave)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chave) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(chave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(EmissorDeToken.EMISSOR));
        return decodificador;
    }

    @Bean
    SecurityFilterChain seguranca(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        boolean consoleH2 = consoleH2Habilitado();
        boolean perfilE2e = ambiente.matchesProfiles("e2e");

        http.csrf(csrf -> {
            csrf.spa();
            if (perfilE2e) {
                csrf.ignoringRequestMatchers(TESTES_E2E);
            }
            if (consoleH2) {
                csrf.ignoringRequestMatchers(H2_CONSOLE);
            }
        });
        http.addFilterAfter(new FiltroDeCookieCsrf(), CsrfFilter.class);

        http.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        http.exceptionHandling(erros -> erros
                .authenticationEntryPoint(entradaDeAutenticacao)
                .accessDeniedHandler(respostaDeAcessoNegado));

        /*
         * O resource server é montado à mão em vez de http.oauth2ResourceServer(): o configurador padrão ignora o
         * CSRF em toda requisição em que o BearerTokenResolver encontra um token, e aqui o token vem de cookie, o que
         * desligaria o CSRF justamente nas escritas autenticadas.
         */
        JwtAuthenticationProvider provedor = new JwtAuthenticationProvider(jwtDecoder);
        provedor.setJwtAuthenticationConverter(conversorDeJwt);
        AuthenticationManager gerenciador = new ProviderManager(provedor);
        BearerTokenAuthenticationFilter filtroDoToken = new BearerTokenAuthenticationFilter(gerenciador);
        filtroDoToken.setBearerTokenResolver(resolvedorDeToken);
        filtroDoToken.setAuthenticationEntryPoint(entradaDeAutenticacao);
        http.addFilterBefore(filtroDoToken, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(rotas -> {
            rotas.requestMatchers(HttpMethod.GET, "/api/livros/**").permitAll();
            rotas.requestMatchers(HttpMethod.POST, "/api/clientes").permitAll();
            rotas.requestMatchers("/api/sessao", "/api/sessao/**").permitAll();
            rotas.requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll();
            rotas.requestMatchers(getForaDaApi()).permitAll();
            if (consoleH2) {
                rotas.requestMatchers(H2_CONSOLE).permitAll();
            }
            if (perfilE2e) {
                rotas.requestMatchers(TESTES_E2E).permitAll();
            }
            rotas.requestMatchers("/api/me/**").hasRole("CLIENTE");
            rotas.requestMatchers("/api/admin/**").hasRole("ADMIN");
            rotas.requestMatchers("/api/**").authenticated();
            rotas.anyRequest().permitAll();
        });

        if (consoleH2) {
            http.headers(cabecalhos -> cabecalhos.frameOptions(quadros -> quadros.sameOrigin()));
        }

        return http.build();
    }

    /** GET de arquivos estáticos e rotas do {@code web}, fora da API e da especificação OpenAPI (DEC-09, DEC-15.4). */
    private static RequestMatcher getForaDaApi() {
        return requisicao -> "GET".equals(requisicao.getMethod())
                && !sob(requisicao, "/api")
                && !sob(requisicao, "/v3");
    }

    private static boolean sob(jakarta.servlet.http.HttpServletRequest requisicao, String prefixo) {
        String caminho = requisicao.getRequestURI().substring(requisicao.getContextPath().length());
        return caminho.equals(prefixo) || caminho.startsWith(prefixo + "/");
    }

    private boolean consoleH2Habilitado() {
        return ambiente.getProperty("spring.h2.console.enabled", Boolean.class, false);
    }
}
