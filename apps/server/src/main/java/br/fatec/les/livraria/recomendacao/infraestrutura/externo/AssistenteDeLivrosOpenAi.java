package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel.ResponseFormat;
import org.springframework.ai.openai.OpenAiChatOptions;

import com.openai.errors.OpenAIIoException;
import com.openai.errors.OpenAIServiceException;
import com.openai.errors.RateLimitException;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FalhaNaChamadaDeIa;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemRecomendado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;
import br.fatec.les.livraria.recomendacao.infraestrutura.PropriedadesIa;

/**
 * Provedor real compatível com a API OpenAI (Gemini, Groq e OpenRouter), via Spring AI (RNF-REC-001, DEC-03). O
 * cliente só é montado quando há chave, sem retry próprio: a {@code CadeiaDeProvedores} decide o que fazer na falha.
 * As respostas são saída estruturada, e toda falha vira {@link FalhaNaChamadaDeIa} com o resultado de
 * {@code chamada_ia}.
 */
public class AssistenteDeLivrosOpenAi implements AssistenteDeLivros {

    static final String SISTEMA_DA_RECOMENDACAO = """
            Você é o assistente de uma livraria on-line. Recebe o perfil de um cliente e uma lista de livros \
            candidatos. Escolha, entre os candidatos, os que combinam com o perfil e ordene do mais ao menos \
            indicado. Para cada um, escreva em português uma justificativa curta, de até 140 caracteres, dirigida \
            ao cliente. Use apenas os ids da lista de candidatos.""";

    static final String SISTEMA_DA_CONVERSA = """
            Você é o assistente de uma livraria on-line e responde em português, de forma curta e cordial. \
            Ajude só com assuntos da loja: livros do catálogo, autores, categorias, preços e dúvidas gerais sobre \
            compra, frete, trocas e pedidos. Para qualquer outro assunto, responda com o tipo FORA_DO_ESCOPO. \
            Use as ferramentas para consultar o catálogo e o histórico do cliente, e cite só livros cujos ids \
            vieram das ferramentas; nunca invente livros. O tipo da resposta é RECOMENDACAO quando sugere livros \
            pelo gosto do cliente, BUSCA quando atende a uma busca, DUVIDA quando esclarece uma dúvida e \
            FORA_DO_ESCOPO quando o assunto não é da loja.""";

    private static final BeanOutputConverter<SaidaDeRecomendacao> CONVERSOR_DA_RECOMENDACAO =
            new BeanOutputConverter<>(SaidaDeRecomendacao.class);
    private static final BeanOutputConverter<SaidaDoAssistente> CONVERSOR_DO_ASSISTENTE =
            new BeanOutputConverter<>(SaidaDoAssistente.class);

    private final String nome;
    private final PropriedadesIa.Provedor provedor;
    private final @Nullable ChatClient cliente;

    public AssistenteDeLivrosOpenAi(String nome, PropriedadesIa.Provedor provedor, Duration tempoLimite) {
        this.nome = Objects.requireNonNull(nome, "nome");
        this.provedor = Objects.requireNonNull(provedor, "provedor");
        this.cliente = provedor.temChave() ? ChatClient.create(OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(provedor.urlBase())
                        .apiKey(provedor.chave())
                        .model(provedor.modelo())
                        .timeout(tempoLimite)
                        .maxRetries(0)
                        .customHeaders(provedor.cabecalhos())
                        .build())
                .build()) : null;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public String modelo() {
        return provedor.modelo();
    }

    @Override
    public boolean temChave() {
        return cliente != null;
    }

    /** Máximo de chamadas HTTP ao provedor por mensagem (DEC-03). */
    public int maxChamadas() {
        return provedor.maxChamadas();
    }

    @Override
    public RespostaDoProvedor<RespostaDeRecomendacao> recomendar(
            PerfilDoCliente perfil, List<LivroCandidato> candidatos) {
        ResponseEntity<ChatResponse, SaidaDeRecomendacao> resposta = responder(
                clienteConfigurado().prompt()
                        .system(SISTEMA_DA_RECOMENDACAO)
                        .user("Perfil do cliente:\n" + descrever(perfil) + "\n\nCandidatos:\n"
                                + descrever(candidatos)),
                SaidaDeRecomendacao.class, CONVERSOR_DA_RECOMENDACAO, false);
        SaidaDeRecomendacao saida = exigir(resposta.entity());
        List<ItemRecomendado> itens = exigir(saida.itens()).stream()
                .map(item -> new ItemRecomendado(exigir(item.livroId()), Objects.requireNonNullElse(
                        item.justificativa(), "")))
                .toList();
        return comTokens(new RespostaDeRecomendacao(itens), resposta.response());
    }

    @Override
    public RespostaDoProvedor<RespostaDoAssistente> conversar(
            PerfilDoCliente perfil,
            List<MensagemAnterior> historico,
            String mensagemNova,
            FerramentasDoAssistente ferramentas) {
        List<Message> anteriores = historico.stream()
                .<Message>map(mensagem -> mensagem.papel() == PapelDaMensagem.CLIENTE
                        ? new UserMessage(mensagem.texto())
                        : new AssistantMessage(mensagem.texto()))
                .toList();
        ResponseEntity<ChatResponse, SaidaDoAssistente> resposta = responder(
                clienteConfigurado().prompt()
                        .system(SISTEMA_DA_CONVERSA + "\n\nPerfil do cliente:\n" + descrever(perfil))
                        .messages(anteriores)
                        .user(mensagemNova)
                        .tools(new FerramentasParaIa(ferramentas)),
                SaidaDoAssistente.class, CONVERSOR_DO_ASSISTENTE, true);
        SaidaDoAssistente saida = exigir(resposta.entity());
        List<Long> citados = saida.livrosCitados() == null ? List.of() : saida.livrosCitados().stream()
                .filter(Objects::nonNull)
                .toList();
        return comTokens(new RespostaDoAssistente(exigir(saida.tipo()), Objects.requireNonNullElse(saida.texto(), ""),
                citados), resposta.response());
    }

    /**
     * Executa a chamada com a saída estruturada no formato do provedor (DEC-03). Com {@code json-schema}, o schema
     * vai no {@code response_format} nativo e as instruções de formato não são acrescentadas ao prompt.
     */
    private <T> ResponseEntity<ChatResponse, T> responder(
            ChatClientRequestSpec requisicao,
            Class<T> tipo,
            BeanOutputConverter<T> conversor,
            boolean comFerramentas) {
        OpenAiChatOptions.Builder opcoes = opcoesDaChamada(comFerramentas, conversor);
        return chamar(() -> {
            var chamada = requisicao.options(opcoes).call();
            return provedor.usaJsonSchema()
                    ? chamada.responseEntity(tipo, especificacao -> especificacao.useProviderStructuredOutput())
                    : chamada.responseEntity(tipo);
        });
    }

    /** Opções que variam por chamada: tool choice quando há tools e o formato nativo da saída estruturada. */
    private OpenAiChatOptions.Builder opcoesDaChamada(boolean comFerramentas, BeanOutputConverter<?> conversor) {
        OpenAiChatOptions.Builder opcoes = OpenAiChatOptions.builder();
        if (comFerramentas && provedor.exigeToolChoice()) {
            opcoes.toolChoice(PropriedadesIa.Provedor.TOOL_CHOICE_REQUIRED);
        }
        if (provedor.usaJsonSchema()) {
            opcoes.responseFormat(ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .jsonSchema(conversor.getJsonSchema())
                    .strict(false)
                    .build());
        }
        return opcoes;
    }

    private ChatClient clienteConfigurado() {
        if (cliente == null) {
            throw new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_HTTP, null, "Provedor " + nome + " sem chave.");
        }
        return cliente;
    }

    /** Executa a chamada e traduz a exceção do SDK ou da conversão para o resultado de {@code chamada_ia}. */
    private <T> T chamar(Chamada<T> chamada) {
        try {
            return chamada.executar();
        } catch (FalhaNaChamadaDeIa falha) {
            throw falha;
        } catch (RuntimeException excecao) {
            throw traduzir(excecao);
        }
    }

    static FalhaNaChamadaDeIa traduzir(RuntimeException excecao) {
        for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
            if (causa instanceof RateLimitException) {
                return new FalhaNaChamadaDeIa(ResultadoDaChamada.COTA_ESGOTADA, 429, mensagem(causa), excecao);
            }
            if (causa instanceof OpenAIServiceException servico) {
                ResultadoDaChamada resultado = servico.statusCode() == 429
                        ? ResultadoDaChamada.COTA_ESGOTADA
                        : ResultadoDaChamada.ERRO_HTTP;
                return new FalhaNaChamadaDeIa(resultado, servico.statusCode(), mensagem(causa), excecao);
            }
            if (causa instanceof InterruptedIOException) {
                return new FalhaNaChamadaDeIa(ResultadoDaChamada.TEMPO_ESGOTADO, null, mensagem(causa), excecao);
            }
        }
        for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
            if (causa instanceof OpenAIIoException || causa instanceof IOException) {
                return new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_DE_REDE, null, mensagem(causa), excecao);
            }
        }
        return new FalhaNaChamadaDeIa(ResultadoDaChamada.RESPOSTA_INVALIDA, null, mensagem(excecao), excecao);
    }

    private static String mensagem(Throwable causa) {
        String mensagem = causa.getMessage();
        return mensagem == null || mensagem.isBlank() ? causa.getClass().getSimpleName() : mensagem;
    }

    private static <T> T exigir(@Nullable T valor) {
        if (valor == null) {
            throw new FalhaNaChamadaDeIa(ResultadoDaChamada.RESPOSTA_INVALIDA, null,
                    "Resposta fora do formato esperado.");
        }
        return valor;
    }

    private static <T> RespostaDoProvedor<T> comTokens(T conteudo, @Nullable ChatResponse resposta) {
        Usage uso = resposta == null ? null : resposta.getMetadata().getUsage();
        if (uso == null) {
            return RespostaDoProvedor.semTokens(conteudo);
        }
        return new RespostaDoProvedor<>(conteudo, positivoOuNulo(uso.getPromptTokens()),
                positivoOuNulo(uso.getCompletionTokens()));
    }

    private static @Nullable Integer positivoOuNulo(@Nullable Integer tokens) {
        return tokens == null || tokens <= 0 ? null : tokens;
    }

    static String descrever(PerfilDoCliente perfil) {
        StringBuilder texto = new StringBuilder();
        texto.append("- Livros comprados: ").append(perfil.livrosComprados().isEmpty() ? "nenhum"
                : perfil.livrosComprados().stream().map(AssistenteDeLivrosOpenAi::descrever)
                        .collect(Collectors.joining("; "))).append('\n');
        texto.append("- Categorias frequentes: ").append(lista(perfil.categoriasFrequentes())).append('\n');
        texto.append("- Autores frequentes: ").append(lista(perfil.autoresFrequentes())).append('\n');
        texto.append("- Avaliações de recomendações: ").append(perfil.avaliacoes().isEmpty() ? "nenhuma"
                : perfil.avaliacoes().stream().map(AssistenteDeLivrosOpenAi::descrever)
                        .collect(Collectors.joining("; ")));
        return texto.toString();
    }

    static String descrever(List<LivroCandidato> candidatos) {
        return candidatos.stream()
                .map(candidato -> "- id " + candidato.id() + ": " + candidato.titulo()
                        + " | autores: " + lista(candidato.autores())
                        + " | categorias: " + lista(candidato.categorias())
                        + " | preço: " + candidato.preco().emReais())
                .collect(Collectors.joining("\n"));
    }

    private static String descrever(LivroComprado livro) {
        return livro.titulo() + " (categorias: " + lista(livro.categorias()) + "; autores: " + lista(livro.autores())
                + ")";
    }

    private static String descrever(AvaliacaoDada avaliacao) {
        return "livro " + avaliacao.livroId() + (avaliacao.positiva() ? " positiva" : " negativa");
    }

    private static String lista(List<String> itens) {
        return itens.isEmpty() ? "nenhum" : String.join(", ", itens);
    }

    @FunctionalInterface
    private interface Chamada<T> {
        T executar();
    }

    /** Saída estruturada da recomendação, convertida pelo Spring AI a partir do JSON do modelo. */
    record SaidaDeRecomendacao(@Nullable List<ItemDaSaida> itens) {
    }

    /** Livro indicado na saída estruturada, com o id de um dos candidatos. */
    record ItemDaSaida(@Nullable Long livroId, @Nullable String justificativa) {
    }

    /** Saída estruturada do chat: tipo, texto e ids dos livros citados. */
    record SaidaDoAssistente(@Nullable TipoDeResposta tipo, @Nullable String texto,
            @Nullable List<Long> livrosCitados) {
    }
}
