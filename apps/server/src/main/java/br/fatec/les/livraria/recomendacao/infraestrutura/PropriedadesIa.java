package br.fatec.les.livraria.recomendacao.infraestrutura;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Configuração dos provedores de IA e do tempo limite de leitura (RNF-REC-001, DEC-03). A chave e a URL ficam só aqui e
 * nunca são expostas pela API. Uma chave vazia significa provedor sem chave: a cadeia o ignora, e a aplicação sobe sem
 * nenhuma variável {@code IA_*}.
 *
 * @param tempoLimite tempo limite de leitura por chamada a um provedor, por padrão 20 segundos
 * @param provedores provedores por nome ({@code gemini}, {@code groq}, {@code openrouter}, {@code nvidia})
 */
@ConfigurationProperties("livraria.ia")
public record PropriedadesIa(@DefaultValue("20s") Duration tempoLimite, Map<String, Provedor> provedores) {

    public PropriedadesIa {
        if (tempoLimite == null || tempoLimite.isZero() || tempoLimite.isNegative()) {
            throw new IllegalArgumentException("livraria.ia.tempo-limite deve ser positivo.");
        }
        provedores = provedores == null ? Map.of() : Map.copyOf(provedores);
    }

    /**
     * Conexão e ajustes de um provedor compatível com a API OpenAI (DEC-03).
     *
     * @param urlBase endereço da API no formato OpenAI
     * @param modelo modelo gratuito que aceita tool calling e saída estruturada
     * @param chave chave da API; vazia quando o provedor não está configurado
     * @param toolChoice {@code auto} (padrão) ou {@code required}; com {@code required}, a primeira volta do chat exige
     *     uma tool nos formatos de texto
     * @param formato {@code ferramenta} (padrão), {@code json-schema} ou {@code instrucoes}, o formato da resposta do
     *     chat
     * @param cabecalhos cabeçalhos HTTP extras enviados em toda chamada ao provedor
     * @param maxChamadas máximo de chamadas HTTP ao provedor por mensagem, por padrão 5
     */
    public record Provedor(
            String urlBase,
            String modelo,
            String chave,
            @DefaultValue("auto") String toolChoice,
            @DefaultValue("ferramenta") String formato,
            Map<String, String> cabecalhos,
            @DefaultValue("5") int maxChamadas) {

        public static final String TOOL_CHOICE_AUTO = "auto";
        public static final String TOOL_CHOICE_REQUIRED = "required";
        public static final String FORMATO_FERRAMENTA = "ferramenta";
        public static final String FORMATO_INSTRUCOES = "instrucoes";
        public static final String FORMATO_JSON_SCHEMA = "json-schema";

        public Provedor {
            if (urlBase == null || urlBase.isBlank()) {
                throw new IllegalArgumentException("livraria.ia.provedores.*.url-base é obrigatório.");
            }
            if (modelo == null || modelo.isBlank()) {
                throw new IllegalArgumentException("livraria.ia.provedores.*.modelo é obrigatório.");
            }
            toolChoice = opcao(toolChoice, TOOL_CHOICE_AUTO, "tool-choice", TOOL_CHOICE_AUTO, TOOL_CHOICE_REQUIRED);
            formato =
                    opcao(
                            formato,
                            FORMATO_FERRAMENTA,
                            "formato",
                            FORMATO_FERRAMENTA,
                            FORMATO_JSON_SCHEMA,
                            FORMATO_INSTRUCOES);
            cabecalhos = cabecalhos == null ? Map.of() : Map.copyOf(cabecalhos);
            if (maxChamadas < 1) {
                throw new IllegalArgumentException("livraria.ia.provedores.*.max-chamadas deve ser pelo menos 1.");
            }
        }

        /** Indica se o provedor tem chave configurada e pode entrar na cadeia (DEC-03). */
        public boolean temChave() {
            return chave != null && !chave.isBlank();
        }

        /** Indica se as tools do chat devem ser obrigatórias na chamada. */
        public boolean exigeToolChoice() {
            return TOOL_CHOICE_REQUIRED.equals(toolChoice);
        }

        /** Indica se a saída estruturada usa o {@code response_format} nativo da API OpenAI. */
        public boolean usaJsonSchema() {
            return FORMATO_JSON_SCHEMA.equals(formato);
        }

        /** Indica se a resposta final do chat vem pela tool {@code responder} (DEC-03). */
        public boolean usaFerramenta() {
            return FORMATO_FERRAMENTA.equals(formato);
        }

        private static String opcao(String valor, String padrao, String nome, String... aceitos) {
            String normalizado = valor == null || valor.isBlank() ? padrao : valor.trim().toLowerCase(Locale.ROOT);
            for (String aceito : aceitos) {
                if (aceito.equals(normalizado)) {
                    return normalizado;
                }
            }
            String opcoes =
                    aceitos.length == 1
                            ? aceitos[0]
                            : String.join(", ", java.util.Arrays.copyOf(aceitos, aceitos.length - 1))
                                    + " ou "
                                    + aceitos[aceitos.length - 1];
            throw new IllegalArgumentException("livraria.ia.provedores.*." + nome + " deve ser " + opcoes + ".");
        }
    }
}
