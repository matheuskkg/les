package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.PropriedadesJwt;
import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.RespostaDeAcessoNegado;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioComoClock;

import lombok.RequiredArgsConstructor;

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
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Spring Security da API (DEC-08, DEC-15.4): JWT HS256 lido do cookie {@code sessao}, CSRF de SPA, rotas públicas e
 * papéis do banco. As sessões são stateless; não há formulário de login nem HTTP Basic.
 *
 * <p>Todas as regras usam {@link PathPatternRequestMatcher}: ele parte do caminho já decodificado pelo servlet, ao
 * contrário de comparar {@code getRequestURI()} cru (que permitiria {@code /%61pi/admin} escapar das regras).
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
        return NimbusJwtEncoder.withSecretKey(chave).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chave, Relogio relogio) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        JwtTimestampValidator validade = new JwtTimestampValidator();
        validade.setClock(new RelogioComoClock(relogio));
        // A sobrecarga com List evita a criação de array genérico (aviso de unchecked) do varargs.
        decodificador.setJwtValidator(
                JwtValidators.createDefaultWithValidators(
                        List.of(new JwtIssuerValidator(EmissorDeToken.EMISSOR), validade)));
        return decodificador;
    }

    /**
     * O mesmo repositório do CSRF de SPA fica disponível para o controller de sessão rotacionar o token no login e no
     * logout (DEC-08).
     */
    @Bean
    CsrfTokenRepository repositorioDeCsrf() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    SecurityFilterChain seguranca(HttpSecurity http, JwtDecoder jwtDecoder, CsrfTokenRepository repositorioDeCsrf)
            throws Exception {
        boolean consoleH2 = consoleH2Habilitado();
        boolean perfilE2e = ambiente.matchesProfiles("e2e");

        http.csrf(csrf -> {
            csrf.spa();
            csrf.csrfTokenRepository(repositorioDeCsrf);
            if (perfilE2e) {
                csrf.ignoringRequestMatchers(PathPatternRequestMatcher.pathPattern(TESTES_E2E));
            }
            if (consoleH2) {
                csrf.ignoringRequestMatchers(PathPatternRequestMatcher.pathPattern(H2_CONSOLE));
            }
        });

        http.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        http.exceptionHandling(erros ->
                erros.authenticationEntryPoint(entradaDeAutenticacao).accessDeniedHandler(respostaDeAcessoNegado));

        /*
         * O resource server é montado à mão em vez de http.oauth2ResourceServer(): o configurador padrão ignora o
         * CSRF em toda requisição em que o BearerTokenResolver encontra um token, e aqui o token vem de cookie, o que
         * desligaria o CSRF justamente nas escritas autenticadas.
         */
        JwtAuthenticationProvider provedor = new JwtAuthenticationProvider(jwtDecoder);
        provedor.setJwtAuthenticationConverter(conversorDeJwt);
        AuthenticationManager gerenciador = new ProviderManager(provedor);
        // Forma atual da API: o resolvedor vai no conversor, que entra no construtor do filtro.
        BearerTokenAuthenticationConverter conversorDeBearer = new BearerTokenAuthenticationConverter();
        conversorDeBearer.setBearerTokenResolver(resolvedorDeToken);
        BearerTokenAuthenticationFilter filtroDoToken =
                new BearerTokenAuthenticationFilter(gerenciador, conversorDeBearer);
        filtroDoToken.setAuthenticationEntryPoint(entradaDeAutenticacao);
        http.addFilterBefore(filtroDoToken, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(rotas -> {
            // Rotas públicas da API, antes das regras gerais de /api/**.
            rotas.requestMatchers(caminho(HttpMethod.GET, "/api/livros/**")).permitAll();
            rotas.requestMatchers(caminho(HttpMethod.POST, "/api/clientes")).permitAll();
            rotas.requestMatchers(caminho(null, "/api/sessao")).permitAll();
            rotas.requestMatchers(caminho(HttpMethod.GET, "/v3/api-docs/**")).permitAll();
            if (perfilE2e) {
                rotas.requestMatchers(caminho(null, TESTES_E2E)).permitAll();
            }
            // Áreas autenticadas; a ordem importa (/api/me e /api/admin antes do catch-all).
            rotas.requestMatchers(caminho(null, "/api/me"), caminho(null, "/api/me/**")).hasRole("CLIENTE");
            rotas.requestMatchers(caminho(null, "/api/admin/**")).hasRole("ADMIN");
            rotas.requestMatchers(caminho(null, "/api/**")).authenticated();
            /*
             * Os padrões de caminho diferenciam maiúsculas de minúsculas. Sem esta regra, /API/... não casaria com
             * /api/** e cairia no anyRequest().permitAll(), que serve a página da SPA com 200. O prefixo em qualquer
             * caixa exige autenticação, como o minúsculo.
             */
            rotas.requestMatchers(caminho(null, "/{api:[aA][pP][iI]}/**")).authenticated();
            // Demais artefatos da documentação OpenAPI.
            rotas.requestMatchers(caminho(null, "/v3/**")).permitAll();
            // Arquivos estáticos e rotas da SPA (DEC-09, DEC-15.4).
            rotas.anyRequest().permitAll();
        });

        if (consoleH2) {
            http.headers(cabecalhos -> cabecalhos.frameOptions(quadros -> quadros.sameOrigin()));
        }

        return http.build();
    }

    private static RequestMatcher caminho(HttpMethod metodo, String padrao) {
        return PathPatternRequestMatcher.pathPattern(metodo, padrao);
    }

    private boolean consoleH2Habilitado() {
        return ambiente.getProperty("spring.h2.console.enabled", Boolean.class, false);
    }
}
