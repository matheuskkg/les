package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/** Registra o resolvedor de {@link Listagem} no Spring MVC (DEC-07). */
@Configuration
@RequiredArgsConstructor
public class ConfiguracaoDeListagens implements WebMvcConfigurer {

    private final ResolvedorDePaginacao resolvedorDePaginacao;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvedores) {
        resolvedores.add(resolvedorDePaginacao);
    }
}
