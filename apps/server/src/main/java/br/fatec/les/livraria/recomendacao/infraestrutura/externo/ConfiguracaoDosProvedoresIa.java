package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.infraestrutura.PropriedadesIa;

/**
 * Um adapter compatível com OpenAI por provedor de {@code livraria.ia.provedores} (RNF-REC-001, DEC-03). Sem chave,
 * o adapter existe mas fica fora da cadeia, e a aplicação sobe sem nenhuma variável {@code IA_*}.
 */
@Configuration
public class ConfiguracaoDosProvedoresIa {

    @Bean
    AssistenteDeLivros assistenteGemini(PropriedadesIa propriedades) {
        return assistente("gemini", propriedades);
    }

    @Bean
    AssistenteDeLivros assistenteGroq(PropriedadesIa propriedades) {
        return assistente("groq", propriedades);
    }

    @Bean
    AssistenteDeLivros assistenteOpenRouter(PropriedadesIa propriedades) {
        return assistente("openrouter", propriedades);
    }

    private static AssistenteDeLivros assistente(String nome, PropriedadesIa propriedades) {
        PropriedadesIa.Provedor provedor = propriedades.provedores().get(nome);
        if (provedor == null) {
            throw new IllegalStateException("livraria.ia.provedores." + nome + " não configurado.");
        }
        return new AssistenteDeLivrosOpenAi(nome, provedor, propriedades.tempoLimite());
    }
}
