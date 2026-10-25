package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.aplicacao.EstadoEmMemoria;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.AssistenteIndisponivel;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ChamadasIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FalhaNaChamadaDeIa;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.aplicacao.regra.ProvedorEmEsperaPorCota;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.OperacaoIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;

/**
 * Cadeia de provedores de IA generativa da vitrine e do chatbot (RNF-REC-001, DEC-03). A ordem e a ativação vêm de
 * {@code provedor_ia}; cada tentativa, com sucesso ou falha, vira uma linha em {@code chamada_ia}. O primeiro
 * provedor ativo, com chave e fora da espera de cota atende; se ele falhar, a cadeia passa ao próximo. Quando todos
 * falham ou nenhum está disponível, lança {@link AssistenteIndisponivel} para o caso de uso decidir o fallback.
 *
 * <p>A lista de provedores fica em cache por 30 segundos (pelo {@link Relogio}); o REC-05 chama
 * {@link #invalidarCache()} ao alterar a ordem ou a ativação. A espera de 5 minutos depois de um HTTP 429 também é
 * só em memória, via {@link ProvedorEmEsperaPorCota}. O reset dos E2E limpa as duas coisas por
 * {@link EstadoEmMemoria} (DEC-09).</p>
 */
@Component
public class CadeiaDeProvedores implements EstadoEmMemoria {

    /** Tempo de vida do cache da tabela {@code provedor_ia} (DEC-03). */
    static final Duration VALIDADE_DO_CACHE = Duration.ofSeconds(30);

    private final ProvedoresIaRepositorio provedores;
    private final ChamadasIaRepositorio chamadas;
    private final Relogio relogio;

    private final ProvedorEmEsperaPorCota emEspera;
    private final Map<String, AssistenteDeLivros> assistentesPorNome;

    private volatile List<ProvedorIa> cache;
    private volatile LocalDateTime cacheValidoAte;

    public CadeiaDeProvedores(
            List<AssistenteDeLivros> assistentes,
            ProvedoresIaRepositorio provedores,
            ChamadasIaRepositorio chamadas,
            Relogio relogio) {
        // Falha na subida se dois adapters tiverem o mesmo nome.
        this.assistentesPorNome = assistentes.stream()
                .collect(Collectors.toUnmodifiableMap(AssistenteDeLivros::nome, Function.identity()));
        this.provedores = provedores;
        this.chamadas = chamadas;
        this.relogio = relogio;
        this.emEspera = new ProvedorEmEsperaPorCota(relogio);
    }

    /**
     * Ordena os candidatos e justifica cada indicação (RNF-REC-001, DEC-03).
     *
     * @param perfil     histórico e preferências do cliente
     * @param candidatos livros ativos e com estoque, já pontuados pelo server
     * @return resposta do provedor que atendeu, com o id da chamada registrada
     */
    public Atendimento<RespostaDeRecomendacao> recomendar(
            PerfilDoCliente perfil, List<LivroCandidato> candidatos) {
        return atender(OperacaoIa.RECOMENDAR,
                assistente -> assistente.recomendar(perfil, candidatos));
    }

    /**
     * Responde à mensagem nova do cliente (RNF-REC-001, DEC-03).
     *
     * @param perfil       histórico e preferências do cliente
     * @param historico    últimas mensagens da conversa ativa
     * @param mensagemNova mensagem que o cliente acabou de enviar
     * @param ferramentas  tools só de leitura do catálogo, do estoque e das compras
     * @return resposta do provedor que atendeu, com o id da chamada registrada
     */
    public Atendimento<RespostaDoAssistente> conversar(
            PerfilDoCliente perfil,
            List<MensagemAnterior> historico,
            String mensagemNova,
            FerramentasDoAssistente ferramentas) {
        return atender(OperacaoIa.CONVERSAR,
                assistente -> assistente.conversar(perfil, historico, mensagemNova, ferramentas));
    }

    /** Descarta o cache da tabela; o REC-05 chama depois de alterar a ordem ou a ativação (DEC-03). */
    public synchronized void invalidarCache() {
        cache = null;
        cacheValidoAte = null;
    }

    /** Limpa o cache e as esperas de cota, usado pelo reset dos E2E (DEC-09). */
    @Override
    public void limpar() {
        invalidarCache();
        emEspera.limpar();
    }

    private <T> Atendimento<T> atender(OperacaoIa operacao, ChamadaAoProvedor<T> chamada) {
        List<ProvedorDisponivel> disponiveis = provedoresDisponiveis();
        int tentativa = 0;

        for (ProvedorDisponivel disponivel : disponiveis) {
            tentativa++;
            LocalDateTime inicio = relogio.agora();
            long inicioNanos = System.nanoTime();

            RespostaDoProvedor<T> resposta;
            try {
                resposta = chamada.executar(disponivel.assistente());
            } catch (FalhaNaChamadaDeIa falha) {
                registrar(disponivel, operacao, inicio, inicioNanos, tentativa, falha.getResultado(),
                        falha.getCodigoHttp(), falha.getMessage(), null, null);
                if (falha.getResultado() == ResultadoDaChamada.COTA_ESGOTADA) {
                    emEspera.registrarCotaEsgotada(disponivel.assistente().nome());
                }
                continue;
            } catch (RuntimeException inesperada) {
                registrar(disponivel, operacao, inicio, inicioNanos, tentativa, ResultadoDaChamada.ERRO_HTTP,
                        null, mensagem(inesperada), null, null);
                continue;
            }
            // O registro do sucesso fica fora do try: uma falha ao gravar o log não pode virar falha do provedor.
            Long chamadaIaId = registrar(disponivel, operacao, inicio, inicioNanos, tentativa,
                    ResultadoDaChamada.SUCESSO, null, null, resposta.tokensEntrada(), resposta.tokensSaida());
            return new Atendimento<>(resposta.conteudo(), chamadaIaId, disponivel.assistente().nome());
        }

        throw new AssistenteIndisponivel();
    }

    private Long registrar(ProvedorDisponivel disponivel, OperacaoIa operacao, LocalDateTime inicio,
            long inicioNanos, int tentativa, ResultadoDaChamada resultado, Integer codigoHttp, String erro,
            Integer tokensEntrada, Integer tokensSaida) {
        return chamadas.registrar(new ChamadaIa(
                disponivel.provedor().id(),
                disponivel.assistente().modelo(),
                operacao,
                inicio,
                latenciaMs(inicioNanos),
                tentativa,
                resultado,
                codigoHttp,
                erro,
                tokensEntrada,
                tokensSaida));
    }

    /** Provedores ativos, com bean de mesmo nome, com chave e fora da espera de 429, na ordem da tabela. */
    private List<ProvedorDisponivel> provedoresDisponiveis() {
        List<ProvedorDisponivel> disponiveis = new ArrayList<>();

        for (ProvedorIa provedor : provedoresEmCache()) {
            if (!provedor.ativo()) {
                continue;
            }
            AssistenteDeLivros assistente = assistentesPorNome.get(provedor.nome());
            if (assistente == null || !assistente.temChave() || emEspera.emEspera(provedor.nome())) {
                continue;
            }
            disponiveis.add(new ProvedorDisponivel(provedor, assistente));
        }
        return disponiveis;
    }

    private List<ProvedorIa> provedoresEmCache() {
        LocalDateTime agora = relogio.agora();
        List<ProvedorIa> atuais = cache;
        if (atuais != null && cacheValidoAte != null && agora.isBefore(cacheValidoAte)) {
            return atuais;
        }

        synchronized (this) {
            atuais = cache;
            if (atuais == null || cacheValidoAte == null || !agora.isBefore(cacheValidoAte)) {
                atuais = List.copyOf(provedores.listarEmOrdem());
                cache = atuais;
                cacheValidoAte = agora.plus(VALIDADE_DO_CACHE);
            }
            return atuais;
        }
    }

    private static int latenciaMs(long inicioNanos) {
        long decorrido = System.nanoTime() - inicioNanos;
        return (int) Math.max(0, decorrido / 1_000_000);
    }

    private static String mensagem(RuntimeException excecao) {
        String mensagem = excecao.getMessage();
        return mensagem == null || mensagem.isBlank() ? excecao.getClass().getSimpleName() : mensagem;
    }

    @FunctionalInterface
    private interface ChamadaAoProvedor<T> {
        RespostaDoProvedor<T> executar(AssistenteDeLivros assistente);
    }

    private record ProvedorDisponivel(ProvedorIa provedor, AssistenteDeLivros assistente) {

        private ProvedorDisponivel {
            Objects.requireNonNull(provedor, "provedor");
            Objects.requireNonNull(assistente, "assistente");
        }
    }
}
