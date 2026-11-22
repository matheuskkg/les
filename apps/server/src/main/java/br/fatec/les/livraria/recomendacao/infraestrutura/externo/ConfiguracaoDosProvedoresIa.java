package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import br.fatec.les.livraria.recomendacao.infraestrutura.PropriedadesIa;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.util.Map;

/**
 * Um adapter compatível com OpenAI por item de {@code livraria.ia.provedores} (RNF-REC-001, DEC-03). O registro é feito
 * na subida do contexto, antes da injeção da cadeia, para que cada item da configuração vire um bean {@code
 * assistente-<nome>} e conviva com o {@code fake}. Sem chave, o adapter existe mas fica fora da cadeia, e a aplicação
 * sobe sem nenhuma variável {@code IA_*}.
 */
@Configuration(proxyBeanMethods = false)
public class ConfiguracaoDosProvedoresIa {

    /**
     * Registra os adapters a partir da configuração, sem {@code @Bean} fixo por provedor. Um provedor novo exige só a
     * entrada em {@code livraria.ia.provedores} e a linha correspondente no seed de {@code provedor_ia}.
     */
    @Bean
    static BeanDefinitionRegistryPostProcessor assistentesDeLivros(Environment ambiente) {
        PropriedadesIa propriedades =
                Binder.get(ambiente)
                        .bind("livraria.ia", Bindable.of(PropriedadesIa.class))
                        .orElseGet(() -> new PropriedadesIa(Duration.ofSeconds(20), Map.of()));

        return new BeanDefinitionRegistryPostProcessor() {

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registro) throws BeansException {
                propriedades
                        .provedores()
                        .forEach((nome, provedor) -> {
                            if (AssistenteDeLivrosFake.NOME.equals(nome)) {
                                throw new IllegalStateException(
                                        "livraria.ia.provedores." + nome + " é reservado ao provedor fake.");
                            }
                            BeanDefinition definicao =
                                    BeanDefinitionBuilder.genericBeanDefinition(AssistenteDeLivrosOpenAi.class)
                                            .addConstructorArgValue(nome)
                                            .addConstructorArgValue(provedor)
                                            .addConstructorArgValue(propriedades.tempoLimite())
                                            .getBeanDefinition();
                            registro.registerBeanDefinition("assistente-" + nome, definicao);
                        });
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                // Os beans já foram registrados em postProcessBeanDefinitionRegistry.
            }
        };
    }
}
