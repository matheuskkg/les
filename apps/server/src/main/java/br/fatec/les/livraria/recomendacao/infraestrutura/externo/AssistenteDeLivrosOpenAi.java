package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FalhaNaChamadaDeIa;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.calculo.DetectarCotaDiaria;
import br.fatec.les.livraria.recomendacao.dominio.calculo.InterpretarEsperaSugerida;
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
import br.fatec.les.livraria.recomendacao.dominio.modelo.VoltaDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.regra.LimiteDeChamadasHttp;
import br.fatec.les.livraria.recomendacao.dominio.regra.LimiteDeCorrecoesDeFormato;
import br.fatec.les.livraria.recomendacao.dominio.regra.LimiteDeVoltasDeTools;
import br.fatec.les.livraria.recomendacao.infraestrutura.PropriedadesIa;

import com.openai.core.http.Headers;
import com.openai.errors.OpenAIIoException;
import com.openai.errors.OpenAIServiceException;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel.ResponseFormat;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Provedor real compatível com a API OpenAI (Gemini, Groq e OpenRouter), via Spring AI (RNF-REC-001, DEC-03). O cliente
 * só é montado quando há chave, sem retry próprio: a {@code CadeiaDeProvedores} decide o que fazer na falha.
 *
 * <p>O loop de tools é controlado pelo adapter, não pelo Spring AI: cada {@code call} faz uma única chamada HTTP, as
 * tools de leitura são executadas pelo {@link ToolCallingManager} e a volta seguinte recebe o histórico devolvido por
 * ele. No formato {@code ferramenta}, a resposta final vem da tool {@code responder}, que encerra o loop sem outra
 * chamada. Os limites de voltas, de correção e de chamadas HTTP ficam em {@link LimiteDeVoltasDeTools}, {@link
 * LimiteDeCorrecoesDeFormato} e {@link LimiteDeChamadasHttp}.
 */
public class AssistenteDeLivrosOpenAi implements AssistenteDeLivros {

    static final String SISTEMA_DA_RECOMENDACAO =
            """
            Você é o assistente de uma livraria on-line. Recebe o perfil de um cliente e uma lista de livros \
            candidatos. Escolha, entre os candidatos, os que combinam com o perfil e ordene do mais ao menos \
            indicado. Para cada um, escreva em português uma justificativa curta, de até 140 caracteres, dirigida \
            ao cliente. Use apenas os ids da lista de candidatos.\
            """;

    static final String SISTEMA_DA_CONVERSA =
            """
            Você é o assistente de uma livraria on-line e responde em português, de forma curta e cordial. \
            Ajude só com assuntos da loja: livros do catálogo, autores, categorias, preços e dúvidas gerais sobre \
            compra, frete, trocas e pedidos. Para qualquer outro assunto, responda com o tipo FORA_DO_ESCOPO. \
            O catálogo à venda está no prompt, com id, título, autores, categorias e preço: use-o para buscas por \
            título, autor, gênero ou faixa de preço. Use verLivro só para a sinopse ou a quantidade em estoque e \
            recomendarParaMim para sugestões pelo histórico do cliente. Cite só ids do catálogo; nunca invente \
            livros. Nunca escreva ids nem códigos internos no texto que o cliente lê: identifique cada livro pelo \
            título e deixe os ids só no campo de ids da resposta. O tipo da resposta é RECOMENDACAO quando sugere \
            livros pelo gosto do cliente, BUSCA quando atende a uma busca, DUVIDA quando esclarece uma dúvida e \
            FORA_DO_ESCOPO quando o assunto não é da loja.\
            """;

    static final String INSTRUCAO_DA_FERRAMENTA_DE_RESPOSTA =
            """
            Sempre entregue a resposta final chamando a ferramenta responder, com o tipo, o texto e os ids dos \
            livros citados. Nunca escreva a resposta no texto da mensagem: o texto serve só para as chamadas das \
            ferramentas de consulta.\
            """;

    private static final BeanOutputConverter<SaidaDeRecomendacao> CONVERSOR_DA_RECOMENDACAO =
            new BeanOutputConverter<>(SaidaDeRecomendacao.class);
    private static final BeanOutputConverter<SaidaDoAssistente> CONVERSOR_DO_ASSISTENTE =
            new BeanOutputConverter<>(SaidaDoAssistente.class);
    private static final FerramentaDeResposta FERRAMENTA_DE_RESPOSTA = new FerramentaDeResposta();

    private final String nome;
    private final PropriedadesIa.Provedor provedor;
    private final @Nullable OpenAiChatModel modelo;
    private final OpenAiChatOptions opcoesBase;
    private final ToolCallingManager gerenciadorDeFerramentas = ToolCallingManager.builder().build();
    private final LimiteDeVoltasDeTools limiteDeVoltasDeTools = new LimiteDeVoltasDeTools();
    private final LimiteDeCorrecoesDeFormato limiteDeCorrecoes = new LimiteDeCorrecoesDeFormato();
    private final LimiteDeChamadasHttp limiteDeChamadasHttp = new LimiteDeChamadasHttp();

    public AssistenteDeLivrosOpenAi(String nome, PropriedadesIa.Provedor provedor, Duration tempoLimite) {
        this.nome = Objects.requireNonNull(nome, "nome");
        this.provedor = Objects.requireNonNull(provedor, "provedor");
        this.opcoesBase =
                OpenAiChatOptions.builder()
                        .baseUrl(provedor.urlBase())
                        .apiKey(provedor.chave())
                        .model(provedor.modelo())
                        .timeout(tempoLimite)
                        .maxRetries(0)
                        .customHeaders(provedor.cabecalhos())
                        .build();
        this.modelo = provedor.temChave() ? OpenAiChatModel.builder().options(opcoesBase).build() : null;
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
        return modelo != null;
    }

    /** Máximo de chamadas HTTP ao provedor por mensagem (DEC-03). */
    public int maxChamadas() {
        return provedor.maxChamadas();
    }

    @Override
    public RespostaDoProvedor<RespostaDeRecomendacao> recomendar(
            PerfilDoCliente perfil, List<LivroCandidato> candidatos) {
        OpenAiChatModel modelo = modeloConfigurado();
        List<VoltaDaChamada> voltas = new ArrayList<>();
        int correcoes = 0;

        String sistema = SISTEMA_DA_RECOMENDACAO;
        if (!provedor.usaJsonSchema()) {
            sistema += "\n\n" + CONVERSOR_DA_RECOMENDACAO.getFormat();
        }
        Prompt prompt =
                new Prompt(
                        List.of(
                                new SystemMessage(sistema),
                                new UserMessage(
                                        "Perfil do cliente:\n"
                                                + descrever(perfil)
                                                + "\n\nCandidatos:\n"
                                                + descrever(candidatos))),
                        opcoesDaRecomendacao());

        try {
            while (true) {
                ChatResponse resposta = chamarHttp(modelo, prompt, voltas);
                String texto = conteudo(resposta);
                try {
                    SaidaDeRecomendacao saida =
                            CONVERSOR_DA_RECOMENDACAO.convert(Objects.requireNonNullElse(texto, ""));
                    return RespostaDoProvedor.comVoltas(new RespostaDeRecomendacao(itens(saida)), voltas);
                } catch (RuntimeException invalida) {
                    Correcao correcao =
                            corrigir(
                                    prompt,
                                    correcoes,
                                    "Resposta fora do formato esperado.",
                                    invalida,
                                    voltas,
                                    atual ->
                                            promptDeCorrecao(
                                                    atual, texto, correcaoDeFormato(invalida), opcoesDaRecomendacao()));
                    prompt = correcao.prompt();
                    correcoes = correcao.feitas();
                }
            }
        } catch (FalhaNaChamadaDeIa falha) {
            throw falha;
        } catch (RuntimeException inesperada) {
            // Qualquer falha inesperada depois de uma chamada HTTP carrega as voltas para o log da cadeia.
            throw traduzir(inesperada, voltas);
        }
    }

    @Override
    public RespostaDoProvedor<RespostaDoAssistente> conversar(
            PerfilDoCliente perfil,
            List<MensagemAnterior> historico,
            String mensagemNova,
            FerramentasDoAssistente ferramentas,
            List<ResumoDeLivro> catalogo) {
        OpenAiChatModel modelo = modeloConfigurado();
        List<VoltaDaChamada> voltas = new ArrayList<>();
        int voltasDeTools = 0;
        int correcoes = 0;
        boolean ferramenta = provedor.usaFerramenta();
        List<ToolCallback> callbacks = callbacks(ferramentas, ferramenta);

        Prompt prompt = promptInicial(perfil, historico, mensagemNova, catalogo, callbacks, ferramenta, 1);
        List<String> nomesDisponiveis =
                callbacks.stream().map(callback -> callback.getToolDefinition().name()).toList();
        try {
            while (true) {
                ChatResponse resposta = chamarHttp(modelo, prompt, voltas);
                AssistantMessage saida = mensagem(resposta);
                List<AssistantMessage.ToolCall> chamadas = chamadas(saida);

                Optional<AssistantMessage.ToolCall> respostaFinal =
                        ferramenta
                                ? chamadas.stream()
                                        .filter(chamada -> FerramentaDeResposta.NOME.equals(chamada.name()))
                                        .findFirst()
                                : Optional.empty();
                if (respostaFinal.isPresent()) {
                    try {
                        return RespostaDoProvedor.comVoltas(
                                respostaDoAssistente(CONVERSOR_DO_ASSISTENTE.convert(respostaFinal.get().arguments())),
                                voltas);
                    } catch (RuntimeException invalida) {
                        Correcao correcao =
                                corrigir(
                                        prompt,
                                        correcoes,
                                        "Resposta da ferramenta responder fora do formato esperado.",
                                        invalida,
                                        voltas,
                                        atual ->
                                                promptDeCorrecao(
                                                        atual,
                                                        saida,
                                                        correcaoDaFerramenta(invalida),
                                                        callbacks,
                                                        ferramenta,
                                                        voltas.size() + 1));
                        prompt = correcao.prompt();
                        correcoes = correcao.feitas();
                        continue;
                    }
                }

                List<AssistantMessage.ToolCall> desconhecidas =
                        chamadas.stream().filter(chamada -> !nomesDisponiveis.contains(chamada.name())).toList();
                if (!desconhecidas.isEmpty()) {
                    Correcao correcao =
                            corrigir(
                                    prompt,
                                    correcoes,
                                    "A resposta chamou uma ferramenta que não existe.",
                                    null,
                                    voltas,
                                    atual ->
                                            promptDeCorrecaoDeFerramentaDesconhecida(
                                                    atual,
                                                    saida,
                                                    desconhecidas,
                                                    nomesDisponiveis,
                                                    callbacks,
                                                    ferramenta,
                                                    voltas.size() + 1));
                    prompt = correcao.prompt();
                    correcoes = correcao.feitas();
                    continue;
                }

                if (!chamadas.isEmpty()) {
                    if (!limiteDeVoltasDeTools.permite(voltasDeTools)) {
                        throw limiteDeChamadas(voltas);
                    }
                    if (!limiteDeChamadasHttp.permite(voltas.size(), provedor.maxChamadas())) {
                        throw limiteDeChamadas(voltas);
                    }
                    voltasDeTools++;
                    ToolExecutionResult resultado;
                    try {
                        resultado = gerenciadorDeFerramentas.executeToolCalls(prompt, resposta);
                    } catch (RuntimeException excecao) {
                        throw traduzir(excecao, voltas);
                    }
                    prompt =
                            new Prompt(
                                    resultado.conversationHistory(),
                                    opcoesDaConversa(callbacks, ferramenta, voltas.size() + 1));
                    continue;
                }

                if (ferramenta) {
                    Correcao correcao =
                            corrigir(
                                    prompt,
                                    correcoes,
                                    "A resposta não chamou a ferramenta responder.",
                                    null,
                                    voltas,
                                    atual ->
                                            promptDeCorrecao(
                                                    atual,
                                                    saida,
                                                    correcaoDaFerramenta(null),
                                                    callbacks,
                                                    ferramenta,
                                                    voltas.size() + 1));
                    prompt = correcao.prompt();
                    correcoes = correcao.feitas();
                    continue;
                }

                try {
                    return RespostaDoProvedor.comVoltas(
                            respostaDoAssistente(
                                    CONVERSOR_DO_ASSISTENTE.convert(
                                            Objects.requireNonNullElse(conteudo(resposta), ""))),
                            voltas);
                } catch (RuntimeException invalida) {
                    Correcao correcao =
                            corrigir(
                                    prompt,
                                    correcoes,
                                    "Resposta fora do formato esperado.",
                                    invalida,
                                    voltas,
                                    atual ->
                                            promptDeCorrecao(
                                                    atual,
                                                    saida,
                                                    correcaoDeFormato(invalida),
                                                    callbacks,
                                                    ferramenta,
                                                    voltas.size() + 1));
                    prompt = correcao.prompt();
                    correcoes = correcao.feitas();
                }
            }
        } catch (FalhaNaChamadaDeIa falha) {
            throw falha;
        } catch (RuntimeException inesperada) {
            // Qualquer falha inesperada depois de uma chamada HTTP carrega as voltas para o log da cadeia.
            throw traduzir(inesperada, voltas);
        }
    }

    private Prompt promptInicial(
            PerfilDoCliente perfil,
            List<MensagemAnterior> historico,
            String mensagemNova,
            List<ResumoDeLivro> catalogo,
            List<ToolCallback> callbacks,
            boolean ferramenta,
            int volta) {
        String sistema =
                SISTEMA_DA_CONVERSA
                        + (ferramenta ? "\n\n" + INSTRUCAO_DA_FERRAMENTA_DE_RESPOSTA : "")
                        + "\n\nCatálogo à venda:\n"
                        + ContextoCompactoDoChat.catalogo(catalogo)
                        + "\n\nPerfil do cliente:\n"
                        + ContextoCompactoDoChat.perfil(perfil, catalogo);
        if (!ferramenta && !provedor.usaJsonSchema()) {
            sistema += "\n\n" + CONVERSOR_DO_ASSISTENTE.getFormat();
        }

        List<Message> mensagens = new ArrayList<>();
        mensagens.add(new SystemMessage(sistema));
        mensagens.addAll(
                historico.stream()
                        .<Message>map(mensagem ->
                                mensagem.papel() == PapelDaMensagem.CLIENTE
                                        ? new UserMessage(mensagem.texto())
                                        : new AssistantMessage(mensagem.texto()))
                        .toList());
        mensagens.add(new UserMessage(mensagemNova));
        return new Prompt(mensagens, opcoesDaConversa(callbacks, ferramenta, volta));
    }

    /** Opções da recomendação: sem tools e com o formato estruturado do provedor, quando houver (DEC-03). */
    private OpenAiChatOptions opcoesDaRecomendacao() {
        OpenAiChatOptions.Builder opcoes = opcoesBase.mutate();
        if (provedor.usaJsonSchema()) {
            opcoes.responseFormat(formatoJsonSchema(CONVERSOR_DA_RECOMENDACAO));
        }
        return opcoes.build();
    }

    /**
     * Opções de uma volta do chat: as tools de leitura e, no formato {@code ferramenta}, a tool de resposta final. O
     * {@code tool_choice} é {@code required} na primeira volta quando configurado e sempre no formato {@code
     * ferramenta}; nas demais voltas fica {@code auto} para o modelo poder responder (DEC-03).
     */
    private OpenAiChatOptions opcoesDaConversa(List<ToolCallback> callbacks, boolean ferramenta, int volta) {
        OpenAiChatOptions.Builder opcoes =
                opcoesBase.mutate().toolCallbacks(callbacks).toolChoice(escolhaDeFerramenta(ferramenta, volta));
        if (provedor.usaJsonSchema()) {
            opcoes.responseFormat(formatoJsonSchema(CONVERSOR_DO_ASSISTENTE));
        }
        return opcoes.build();
    }

    private String escolhaDeFerramenta(boolean ferramenta, int volta) {
        if (ferramenta || (volta == 1 && provedor.exigeToolChoice())) {
            return PropriedadesIa.Provedor.TOOL_CHOICE_REQUIRED;
        }
        return PropriedadesIa.Provedor.TOOL_CHOICE_AUTO;
    }

    private static List<ToolCallback> callbacks(FerramentasDoAssistente ferramentas, boolean ferramenta) {
        List<ToolCallback> callbacks = new ArrayList<>();
        Collections.addAll(callbacks, ToolCallbacks.from(new FerramentasParaIa(ferramentas)));
        if (ferramenta) {
            Collections.addAll(callbacks, ToolCallbacks.from(FERRAMENTA_DE_RESPOSTA));
        }
        return List.copyOf(callbacks);
    }

    private static ResponseFormat formatoJsonSchema(BeanOutputConverter<?> conversor) {
        return ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(conversor.getJsonSchema())
                .strict(false)
                .build();
    }

    private ChatResponse chamarHttp(OpenAiChatModel modelo, Prompt prompt, List<VoltaDaChamada> voltas) {
        if (!limiteDeChamadasHttp.permite(voltas.size(), provedor.maxChamadas())) {
            throw limiteDeChamadas(voltas);
        }
        try {
            ChatResponse resposta = modelo.call(prompt);
            voltas.add(volta(resposta));
            return resposta;
        } catch (FalhaNaChamadaDeIa falha) {
            throw falha;
        } catch (RuntimeException excecao) {
            voltas.add(VoltaDaChamada.semTokens());
            throw traduzir(excecao, voltas);
        }
    }

    private static VoltaDaChamada volta(@Nullable ChatResponse resposta) {
        if (resposta == null || resposta.getMetadata() == null || resposta.getMetadata().getUsage() == null) {
            return VoltaDaChamada.semTokens();
        }
        Usage uso = resposta.getMetadata().getUsage();
        return new VoltaDaChamada(positivoOuNulo(uso.getPromptTokens()), positivoOuNulo(uso.getCompletionTokens()));
    }

    /**
     * Aplica a única correção de formato permitida e monta o prompt seguinte (RNF-REC-001, DEC-03). O limite de
     * chamadas HTTP não é repetido aqui: {@link #chamarHttp} recusa a próxima chamada quando o total do provedor
     * acabou, mantendo o resultado {@code LIMITE_DE_CHAMADAS}.
     */
    private Correcao corrigir(
            Prompt prompt,
            int correcoes,
            String mensagem,
            @Nullable Throwable causa,
            List<VoltaDaChamada> voltas,
            UnaryOperator<Prompt> montarPrompt) {
        if (!limiteDeCorrecoes.permite(correcoes)) {
            throw respostaInvalida(mensagem, causa, voltas);
        }
        return new Correcao(montarPrompt.apply(prompt), correcoes + 1);
    }

    private record Correcao(Prompt prompt, int feitas) {}

    private static Prompt promptDeCorrecao(
            Prompt prompt, @Nullable String textoAnterior, String correcao, OpenAiChatOptions opcoes) {
        List<Message> mensagens = new ArrayList<>(prompt.getInstructions());
        mensagens.add(new AssistantMessage(Objects.requireNonNullElse(textoAnterior, "")));
        mensagens.add(new UserMessage(correcao));
        return new Prompt(mensagens, opcoes);
    }

    /**
     * Corrige uma resposta que não chamou a tool de resposta final. Como a resposta pode ter chamado tools, o histórico
     * leva a mensagem do assistente e uma resposta de erro para cada chamada antes da mensagem de correção, senão a
     * próxima requisição fica inválida para a API OpenAI (DEC-03).
     */
    private Prompt promptDeCorrecao(
            Prompt prompt,
            @Nullable AssistantMessage saida,
            String correcao,
            List<ToolCallback> callbacks,
            boolean ferramenta,
            int volta) {
        return promptDeCorrecao(
                prompt,
                saida,
                correcao,
                callbacks,
                ferramenta,
                volta,
                chamada ->
                        FerramentaDeResposta.NOME.equals(chamada.name())
                                ? "Erro: " + correcao
                                : "Não executada: a resposta final veio na ferramenta responder.");
    }

    private Prompt promptDeCorrecao(
            Prompt prompt,
            @Nullable AssistantMessage saida,
            String correcao,
            List<ToolCallback> callbacks,
            boolean ferramenta,
            int volta,
            Function<AssistantMessage.ToolCall, String> respostaDeErro) {
        List<Message> mensagens = new ArrayList<>(prompt.getInstructions());
        if (saida == null) {
            mensagens.add(new AssistantMessage(""));
        } else {
            mensagens.add(saida);
            if (saida.hasToolCalls()) {
                List<ToolResponse> respostas =
                        saida.getToolCalls().stream()
                                .map(chamada ->
                                        new ToolResponse(chamada.id(), chamada.name(), respostaDeErro.apply(chamada)))
                                .toList();
                mensagens.add(ToolResponseMessage.builder().responses(respostas).build());
            }
        }
        mensagens.add(new UserMessage(correcao));
        return new Prompt(mensagens, opcoesDaConversa(callbacks, ferramenta, volta));
    }

    /**
     * Corrige uma chamada de ferramenta que não está entre as anunciadas ao modelo (DEC-03). A mensagem lista as
     * ferramentas disponíveis e o histórico responde às chamadas com erro, para a próxima requisição ser válida.
     */
    private Prompt promptDeCorrecaoDeFerramentaDesconhecida(
            Prompt prompt,
            @Nullable AssistantMessage saida,
            List<AssistantMessage.ToolCall> desconhecidas,
            List<String> disponiveis,
            List<ToolCallback> callbacks,
            boolean ferramenta,
            int volta) {
        String correcao =
                "A resposta chamou ferramentas que não existem: "
                        + desconhecidas.stream()
                                .map(AssistantMessage.ToolCall::name)
                                .distinct()
                                .collect(Collectors.joining(", "))
                        + ". Ferramentas disponíveis: "
                        + String.join(", ", disponiveis)
                        + ". Responda chamando apenas as ferramentas disponíveis"
                        + (ferramenta ? " e entregue a resposta final com a ferramenta responder." : ".");
        return promptDeCorrecao(
                prompt,
                saida,
                correcao,
                callbacks,
                ferramenta,
                volta,
                chamada ->
                        desconhecidas.contains(chamada)
                                ? "Erro: a ferramenta "
                                        + chamada.name()
                                        + " não existe. Ferramentas disponíveis: "
                                        + String.join(", ", disponiveis)
                                        + "."
                                : "Não executada: a resposta chamou uma ferramenta desconhecida.");
    }

    private static String correcaoDeFormato(@Nullable RuntimeException erro) {
        return "A resposta anterior não seguiu o formato esperado"
                + (erro == null ? "" : " (" + mensagem(erro) + ")")
                + ". Responda de novo somente com o JSON pedido.";
    }

    private static String correcaoDaFerramenta(@Nullable RuntimeException erro) {
        return "A resposta anterior não chamou a ferramenta responder no formato esperado"
                + (erro == null ? "" : " (" + mensagem(erro) + ")")
                + ". Responda chamando responder com o tipo, o texto e os ids dos livros citados.";
    }

    private static RespostaDoAssistente respostaDoAssistente(SaidaDoAssistente saida) {
        if (saida.tipo() == null) {
            throw new IllegalArgumentException("tipo ausente na resposta.");
        }
        List<Long> citados =
                saida.livrosCitados() == null
                        ? List.of()
                        : saida.livrosCitados().stream().filter(Objects::nonNull).toList();
        return new RespostaDoAssistente(saida.tipo(), Objects.requireNonNullElse(saida.texto(), ""), citados);
    }

    private static List<ItemRecomendado> itens(SaidaDeRecomendacao saida) {
        if (saida.itens() == null) {
            throw new IllegalArgumentException("itens ausentes na resposta.");
        }
        return saida.itens().stream()
                .map(item -> {
                    if (item.livroId() == null) {
                        throw new IllegalArgumentException("livroId ausente na resposta.");
                    }
                    return new ItemRecomendado(item.livroId(), Objects.requireNonNullElse(item.justificativa(), ""));
                })
                .toList();
    }

    private static @Nullable AssistantMessage mensagem(@Nullable ChatResponse resposta) {
        if (resposta == null || resposta.getResult() == null) {
            return null;
        }
        return resposta.getResult().getOutput();
    }

    private static List<AssistantMessage.ToolCall> chamadas(@Nullable AssistantMessage mensagem) {
        return mensagem == null || mensagem.getToolCalls() == null ? List.of() : mensagem.getToolCalls();
    }

    private static @Nullable String conteudo(@Nullable ChatResponse resposta) {
        AssistantMessage mensagem = mensagem(resposta);
        return mensagem == null ? null : mensagem.getText();
    }

    private OpenAiChatModel modeloConfigurado() {
        if (modelo == null) {
            throw new FalhaNaChamadaDeIa(
                    ResultadoDaChamada.ERRO_HTTP, null, "Provedor " + nome + " sem chave.", List.of());
        }
        return modelo;
    }

    private static FalhaNaChamadaDeIa limiteDeChamadas(List<VoltaDaChamada> voltas) {
        return new FalhaNaChamadaDeIa(
                ResultadoDaChamada.LIMITE_DE_CHAMADAS,
                null,
                "Limite de chamadas ao provedor por mensagem atingido.",
                List.copyOf(voltas));
    }

    private static FalhaNaChamadaDeIa respostaInvalida(
            String mensagem, @Nullable Throwable causa, List<VoltaDaChamada> voltas) {
        return new FalhaNaChamadaDeIa(ResultadoDaChamada.RESPOSTA_INVALIDA, null, mensagem, List.copyOf(voltas), causa);
    }

    static FalhaNaChamadaDeIa traduzir(RuntimeException excecao) {
        return traduzir(excecao, List.of());
    }

    static FalhaNaChamadaDeIa traduzir(RuntimeException excecao, List<VoltaDaChamada> voltas) {
        List<VoltaDaChamada> feitas = List.copyOf(voltas);
        for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
            if (causa instanceof OpenAIServiceException servico) {
                if (servico.statusCode() == 429) {
                    String mensagem = mensagem(causa);
                    Duration espera =
                            InterpretarEsperaSugerida.deCabecalho(
                                            cabecalho(servico.headers(), "Retry-After"), Instant.now())
                                    .or(() -> InterpretarEsperaSugerida.daMensagem(mensagem))
                                    .orElse(null);
                    return new FalhaNaChamadaDeIa(
                            ResultadoDaChamada.COTA_ESGOTADA,
                            429,
                            mensagem,
                            feitas,
                            excecao,
                            espera,
                            DetectarCotaDiaria.ehDiaria(mensagem, espera));
                }
                return new FalhaNaChamadaDeIa(
                        ResultadoDaChamada.ERRO_HTTP, servico.statusCode(), mensagem(causa), feitas, excecao);
            }
            if (causa instanceof InterruptedIOException) {
                return new FalhaNaChamadaDeIa(
                        ResultadoDaChamada.TEMPO_ESGOTADO, null, mensagem(causa), feitas, excecao);
            }
        }
        for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
            if (causa instanceof OpenAIIoException || causa instanceof IOException) {
                return new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_DE_REDE, null, mensagem(causa), feitas, excecao);
            }
        }
        return new FalhaNaChamadaDeIa(ResultadoDaChamada.RESPOSTA_INVALIDA, null, mensagem(excecao), feitas, excecao);
    }

    /** Valor do cabeçalho HTTP, procurado sem diferenciar maiúsculas. */
    private static @Nullable String cabecalho(Headers cabecalhos, String nome) {
        for (String atual : cabecalhos.names()) {
            if (atual.equalsIgnoreCase(nome)) {
                List<String> valores = cabecalhos.values(atual);
                return valores.isEmpty() ? null : valores.getFirst();
            }
        }
        return null;
    }

    private static String mensagem(Throwable causa) {
        String mensagem = causa.getMessage();
        return mensagem == null || mensagem.isBlank() ? causa.getClass().getSimpleName() : mensagem;
    }

    private static @Nullable Integer positivoOuNulo(@Nullable Integer tokens) {
        return tokens == null || tokens <= 0 ? null : tokens;
    }

    static String descrever(PerfilDoCliente perfil) {
        StringBuilder texto = new StringBuilder();
        texto.append("- Livros comprados: ")
                .append(
                        perfil.livrosComprados().isEmpty()
                                ? "nenhum"
                                : perfil.livrosComprados().stream()
                                        .map(AssistenteDeLivrosOpenAi::descrever)
                                        .collect(Collectors.joining("; ")))
                .append('\n');
        texto.append("- Categorias frequentes: ").append(lista(perfil.categoriasFrequentes())).append('\n');
        texto.append("- Autores frequentes: ").append(lista(perfil.autoresFrequentes())).append('\n');
        texto.append("- Avaliações de recomendações: ")
                .append(
                        perfil.avaliacoes().isEmpty()
                                ? "nenhuma"
                                : perfil.avaliacoes().stream()
                                        .map(AssistenteDeLivrosOpenAi::descrever)
                                        .collect(Collectors.joining("; ")));
        return texto.toString();
    }

    static String descrever(List<LivroCandidato> candidatos) {
        return candidatos.stream()
                .map(candidato ->
                        "- id "
                                + candidato.id()
                                + ": "
                                + candidato.titulo()
                                + " | autores: "
                                + lista(candidato.autores())
                                + " | categorias: "
                                + lista(candidato.categorias())
                                + " | preço: "
                                + candidato.preco().emReais())
                .collect(Collectors.joining("\n"));
    }

    private static String descrever(LivroComprado livro) {
        return livro.titulo()
                + " (categorias: "
                + lista(livro.categorias())
                + "; autores: "
                + lista(livro.autores())
                + ")";
    }

    private static String descrever(AvaliacaoDada avaliacao) {
        return "livro " + avaliacao.livroId() + (avaliacao.positiva() ? " positiva" : " negativa");
    }

    private static String lista(List<String> itens) {
        return itens.isEmpty() ? "nenhum" : String.join(", ", itens);
    }

    /** Saída estruturada da recomendação, convertida pelo Spring AI a partir do JSON do modelo. */
    record SaidaDeRecomendacao(@Nullable List<ItemDaSaida> itens) {}

    /** Livro indicado na saída estruturada, com o id de um dos candidatos. */
    record ItemDaSaida(@Nullable Long livroId, @Nullable String justificativa) {}

    /** Saída estruturada do chat: tipo, texto e ids dos livros citados. */
    record SaidaDoAssistente(
            @Nullable TipoDeResposta tipo, @Nullable String texto, @Nullable List<Long> livrosCitados) {}
}
