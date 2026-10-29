package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.CadeiaDeProvedores;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ChamadasIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

/**
 * Configuração dos adapters por item de {@code livraria.ia.provedores} (RNF-REC-001, DEC-03): cada provedor vira um
 * bean e a cadeia recebe todos eles junto com o {@code fake}.
 */
class ConfiguracaoDosProvedoresIaTest {

    private static final String[] PROVEDORES = {
            "livraria.ia.provedores.gemini.url-base=http://127.0.0.1:1/v1",
            "livraria.ia.provedores.gemini.modelo=modelo-gemini",
            "livraria.ia.provedores.groq.url-base=http://127.0.0.1:1/v1",
            "livraria.ia.provedores.groq.modelo=modelo-groq",
            "livraria.ia.provedores.openrouter.url-base=http://127.0.0.1:1/v1",
            "livraria.ia.provedores.openrouter.modelo=modelo-openrouter"};

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(ConfiguracaoDosProvedoresIa.class)
            .withBean(AssistenteDeLivrosFake.class)
            .withBean(CadeiaDeProvedores.class)
            .withBean(ProvedoresIaRepositorio.class, ProvedoresVazios::new)
            .withBean(ChamadasIaRepositorio.class, ChamadasVazias::new)
            .withBean(Relogio.class, RelogioDeTeste::new);

    @Test
    void rnfRec001_todosOsProvedoresConfiguradosMaisOFakeChegamACadeia() {
        contexto.withPropertyValues(PROVEDORES).run(gerenciador -> {
            Map<String, AssistenteDeLivros> assistentes = gerenciador.getBeansOfType(AssistenteDeLivros.class);

            assertThat(assistentes.values()).extracting(AssistenteDeLivros::nome)
                    .containsExactlyInAnyOrder("gemini", "groq", "openrouter", "fake");
            assertThat(gerenciador).hasBean("assistente-gemini")
                    .hasBean("assistente-groq")
                    .hasBean("assistente-openrouter");

            CadeiaDeProvedores cadeia = gerenciador.getBean(CadeiaDeProvedores.class);
            assertThat(assistentesNaCadeia(cadeia)).containsKeys("gemini", "groq", "openrouter", "fake");
        });
    }

    @Test
    void rnfRec001_provedorNovoSomenteNaConfiguracaoViraAdapter() {
        contexto.withPropertyValues(PROVEDORES)
                .withPropertyValues(
                        "livraria.ia.provedores.extra.url-base=http://127.0.0.1:1/v1",
                        "livraria.ia.provedores.extra.modelo=modelo-extra",
                        "livraria.ia.provedores.extra.chave=chave-extra")
                .run(gerenciador -> {
                    assertThat(gerenciador).hasBean("assistente-extra");

                    AssistenteDeLivros extra = gerenciador.getBean("assistente-extra", AssistenteDeLivros.class);
                    assertThat(extra).isInstanceOf(AssistenteDeLivrosOpenAi.class);
                    assertThat(extra.nome()).isEqualTo("extra");
                    assertThat(extra.modelo()).isEqualTo("modelo-extra");
                    assertThat(extra.temChave()).isTrue();
                });
    }

    @SuppressWarnings("unchecked")
    private static Map<String, AssistenteDeLivros> assistentesNaCadeia(CadeiaDeProvedores cadeia) {
        return (Map<String, AssistenteDeLivros>) ReflectionTestUtils.getField(cadeia, "assistentesPorNome");
    }

    private static final class ProvedoresVazios implements ProvedoresIaRepositorio {

        @Override
        public List<ProvedorIa> listarEmOrdem() {
            return List.of();
        }

        @Override
        public List<ProvedorIa> salvar(List<ProvedorIa> provedores) {
            return List.copyOf(provedores);
        }
    }

    private static final class ChamadasVazias implements ChamadasIaRepositorio {

        @Override
        public Long registrar(ChamadaIa chamada) {
            return 1L;
        }
    }

    private static final class RelogioDeTeste implements Relogio {

        @Override
        public LocalDateTime agora() {
            return LocalDateTime.of(2026, 10, 28, 10, 0);
        }
    }
}
