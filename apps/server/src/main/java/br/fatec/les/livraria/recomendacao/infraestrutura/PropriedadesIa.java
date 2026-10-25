package br.fatec.les.livraria.recomendacao.infraestrutura;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuração dos provedores de IA e do tempo limite de leitura (RNF-REC-001, DEC-03). A chave e a URL ficam só
 * aqui e nunca são expostas pela API. Uma chave vazia significa provedor sem chave: a cadeia o ignora, e a
 * aplicação sobe sem nenhuma variável {@code IA_*}.
 *
 * @param tempoLimite tempo limite de leitura por chamada a um provedor, por padrão 20 segundos
 * @param provedores  provedores por nome ({@code gemini}, {@code groq}, {@code openrouter})
 */
@ConfigurationProperties("livraria.ia")
public record PropriedadesIa(
        @DefaultValue("20s") Duration tempoLimite,
        Map<String, Provedor> provedores) {

    public PropriedadesIa {
        if (tempoLimite == null || tempoLimite.isZero() || tempoLimite.isNegative()) {
            throw new IllegalArgumentException("livraria.ia.tempo-limite deve ser positivo.");
        }
        provedores = provedores == null ? Map.of() : Map.copyOf(provedores);
    }

    /**
     * Conexão de um provedor compatível com a API OpenAI (DEC-03).
     *
     * @param urlBase endereço da API no formato OpenAI
     * @param modelo  modelo gratuito que aceita tool calling e saída estruturada
     * @param chave   chave da API; vazia quando o provedor não está configurado
     */
    public record Provedor(String urlBase, String modelo, String chave) {

        public Provedor {
            if (urlBase == null || urlBase.isBlank()) {
                throw new IllegalArgumentException("livraria.ia.provedores.*.url-base é obrigatório.");
            }
            if (modelo == null || modelo.isBlank()) {
                throw new IllegalArgumentException("livraria.ia.provedores.*.modelo é obrigatório.");
            }
        }

        /** Indica se o provedor tem chave configurada e pode entrar na cadeia (DEC-03). */
        public boolean temChave() {
            return chave != null && !chave.isBlank();
        }
    }
}
