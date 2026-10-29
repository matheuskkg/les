package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.AssistenteIndisponivel;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ChamadasIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.DetalheDoLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FalhaNaChamadaDeIa;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.LivroDoHistorico;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ChamadaIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.OperacaoIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;
import br.fatec.les.livraria.recomendacao.infraestrutura.externo.AssistenteDeLivrosFake;

/**
 * Cadeia de provedores sem Spring (RNF-REC-001, DEC-03): ordem, filtros, falhas, espera de cota, cache e registro
 * em {@code chamada_ia}.
 */
class CadeiaDeProvedoresTest {

    private static final PerfilDoCliente PERFIL = new PerfilDoCliente(List.of(), List.of(), List.of(), List.of());
    private static final List<LivroCandidato> CANDIDATOS = List.of(new LivroCandidato(
            1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"), Dinheiro.de("29.90"),
            BigDecimal.ONE));
    private static final FerramentasDoAssistente FERRAMENTAS = new FerramentasVazias();

    private final RelogioDeTeste relogio = new RelogioDeTeste();
    private final ProvedoresEmMemoria provedores = new ProvedoresEmMemoria();
    private final ChamadasEmMemoria chamadas = new ChamadasEmMemoria();

    @Test
    void rnfRec001_segueAOrdemDosProvedoresAtivos() {
        provedores.com(provedor(2L, "b", 2, true), provedor(1L, "a", 1, true));
        AssistenteFalso a = new AssistenteFalso("a");
        AssistenteFalso b = new AssistenteFalso("b");

        Atendimento<RespostaDeRecomendacao> atendimento = cadeia(a, b).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("a");
        assertThat(chamadas.todas()).hasSize(1);
        ChamadaIa chamada = chamadas.todas().get(0);
        assertThat(chamada.provedorIaId()).isEqualTo(1L);
        assertThat(chamada.modelo()).isEqualTo("modelo-a");
        assertThat(chamada.operacao()).isEqualTo(OperacaoIa.RECOMENDAR);
        assertThat(chamada.iniciadaEm()).isEqualTo(relogio.agora());
        assertThat(chamada.latenciaMs()).isGreaterThanOrEqualTo(0);
        assertThat(chamada.tentativa()).isEqualTo(1);
        assertThat(chamada.resultado()).isEqualTo(ResultadoDaChamada.SUCESSO);
        assertThat(chamada.codigoHttp()).isNull();
        assertThat(chamada.erro()).isNull();
        assertThat(chamada.tokensEntrada()).isNull();
        assertThat(chamada.tokensSaida()).isNull();
    }

    @Test
    void rnfRec001_pulaProvedorInativo() {
        provedores.com(provedor(1L, "a", 1, false), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a");
        AssistenteFalso b = new AssistenteFalso("b");

        Atendimento<RespostaDeRecomendacao> atendimento = cadeia(a, b).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("b");
        assertThat(chamadas.todas()).hasSize(1);
        assertThat(chamadas.todas().get(0).provedorIaId()).isEqualTo(2L);
    }

    @Test
    void rnfRec001_pulaProvedorSemChave() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a", false);
        AssistenteFalso b = new AssistenteFalso("b");

        Atendimento<RespostaDeRecomendacao> atendimento = cadeia(a, b).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("b");
        assertThat(chamadas.todas()).hasSize(1);
        assertThat(chamadas.todas().get(0).provedorIaId()).isEqualTo(2L);
    }

    @Test
    void rnfRec001_pulaLinhaSemBean() {
        provedores.com(provedor(1L, "sem-bean", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso b = new AssistenteFalso("b");

        Atendimento<RespostaDeRecomendacao> atendimento = cadeia(b).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("b");
        assertThat(chamadas.todas()).hasSize(1);
        assertThat(chamadas.todas().get(0).provedorIaId()).isEqualTo(2L);
    }

    @Test
    void rnfRec001_falhaPassaAoProximoERegistraAsDuasTentativas() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a")
                .falharCom(new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_HTTP, 500, "indisponível"));
        AssistenteFalso b = new AssistenteFalso("b");

        Atendimento<RespostaDeRecomendacao> atendimento = cadeia(a, b).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("b");
        assertThat(atendimento.chamadaIaId()).isEqualTo(2L);
        assertThat(chamadas.todas()).hasSize(2);

        ChamadaIa primeira = chamadas.todas().get(0);
        assertThat(primeira.provedorIaId()).isEqualTo(1L);
        assertThat(primeira.tentativa()).isEqualTo(1);
        assertThat(primeira.resultado()).isEqualTo(ResultadoDaChamada.ERRO_HTTP);
        assertThat(primeira.codigoHttp()).isEqualTo(500);
        assertThat(primeira.erro()).isEqualTo("indisponível");

        ChamadaIa segunda = chamadas.todas().get(1);
        assertThat(segunda.provedorIaId()).isEqualTo(2L);
        assertThat(segunda.tentativa()).isEqualTo(2);
        assertThat(segunda.resultado()).isEqualTo(ResultadoDaChamada.SUCESSO);
    }

    @Test
    void rnfRec001_erroDaFalhaEhTruncadoEm500Caracteres() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a")
                .falharCom(new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_HTTP, 500, "x".repeat(600)));
        AssistenteFalso b = new AssistenteFalso("b");

        cadeia(a, b).recomendar(PERFIL, CANDIDATOS);

        assertThat(chamadas.todas().get(0).erro()).hasSize(ChamadaIa.LIMITE_DO_ERRO);
    }

    @Test
    void rnfRec001_falhaDoProvedorGeraWarnComDadosDaChamadaSemChaveNemPrompt() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a")
                .falharCom(new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_HTTP, 500, "x".repeat(600)));
        AssistenteFalso b = new AssistenteFalso("b");
        ListAppender<ILoggingEvent> log = capturarLogDaCadeia();

        try {
            cadeia(a, b).recomendar(PERFIL, CANDIDATOS);
        } finally {
            loggerDaCadeia().detachAppender(log);
        }

        assertThat(log.list).anySatisfy(evento -> {
            assertThat(evento.getLevel()).isEqualTo(Level.WARN);
            assertThat(evento.getFormattedMessage())
                    .contains("provedor=a", "modelo=modelo-a", "resultado=ERRO_HTTP", "codigoHttp=500")
                    .contains("x".repeat(ChamadaIa.LIMITE_DO_ERRO))
                    .doesNotContain("x".repeat(ChamadaIa.LIMITE_DO_ERRO + 1));
        });
    }

    @Test
    void rnfRec001_excecaoInesperadaTambemGeraWarn() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a").falharCom(new IllegalStateException("quebrou"));
        AssistenteFalso b = new AssistenteFalso("b");
        ListAppender<ILoggingEvent> log = capturarLogDaCadeia();

        try {
            cadeia(a, b).recomendar(PERFIL, CANDIDATOS);
        } finally {
            loggerDaCadeia().detachAppender(log);
        }

        assertThat(log.list).anySatisfy(evento -> {
            assertThat(evento.getLevel()).isEqualTo(Level.WARN);
            assertThat(evento.getFormattedMessage())
                    .contains("provedor=a", "modelo=modelo-a", "resultado=ERRO_HTTP", "codigoHttp=null",
                            "erro=quebrou");
        });
    }

    private static ListAppender<ILoggingEvent> capturarLogDaCadeia() {
        ListAppender<ILoggingEvent> log = new ListAppender<>();
        log.start();
        loggerDaCadeia().addAppender(log);
        return log;
    }

    private static Logger loggerDaCadeia() {
        return (Logger) LoggerFactory.getLogger(CadeiaDeProvedores.class);
    }

    @Test
    void rnfRec001_excecaoInesperadaViraErroHttpSemCodigo() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a").falharCom(new IllegalStateException("quebrou"));
        AssistenteFalso b = new AssistenteFalso("b");

        Atendimento<RespostaDeRecomendacao> atendimento = cadeia(a, b).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("b");
        ChamadaIa primeira = chamadas.todas().get(0);
        assertThat(primeira.resultado()).isEqualTo(ResultadoDaChamada.ERRO_HTTP);
        assertThat(primeira.codigoHttp()).isNull();
        assertThat(primeira.erro()).isEqualTo("quebrou");
    }

    @Test
    void rnfRec001_cotaEsgotadaTiraOProvedorPorCincoMinutos() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a")
                .falharCom(new FalhaNaChamadaDeIa(ResultadoDaChamada.COTA_ESGOTADA, 429, "cota esgotada"));
        AssistenteFalso b = new AssistenteFalso("b");
        CadeiaDeProvedores cadeia = cadeia(a, b);

        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("b");

        relogio.avancar(Duration.ofMinutes(4).plusSeconds(59));
        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("b");

        relogio.avancar(Duration.ofSeconds(1));
        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("a");

        assertThat(chamadas.todas()).extracting(ChamadaIa::resultado)
                .containsExactly(ResultadoDaChamada.COTA_ESGOTADA, ResultadoDaChamada.SUCESSO,
                        ResultadoDaChamada.SUCESSO, ResultadoDaChamada.SUCESSO);
    }

    @Test
    void rnfRec001_cacheDeTrintaSegundosSoRecarregaDepoisDoPrazo() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, false));
        AssistenteFalso a = new AssistenteFalso("a");
        AssistenteFalso b = new AssistenteFalso("b");
        CadeiaDeProvedores cadeia = cadeia(a, b);

        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("a");

        provedores.com(provedor(1L, "a", 1, false), provedor(2L, "b", 2, true));
        relogio.avancar(Duration.ofSeconds(29));
        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("a");

        relogio.avancar(Duration.ofSeconds(2));
        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("b");
    }

    @Test
    void rnfRec001_invalidarCacheRecarregaNaHora() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, false));
        AssistenteFalso a = new AssistenteFalso("a");
        AssistenteFalso b = new AssistenteFalso("b");
        CadeiaDeProvedores cadeia = cadeia(a, b);

        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("a");

        provedores.com(provedor(1L, "a", 1, false), provedor(2L, "b", 2, true));
        cadeia.invalidarCache();

        assertThat(cadeia.recomendar(PERFIL, CANDIDATOS).provedor()).isEqualTo("b");
    }

    @Test
    void rnfRec001_todaACadeiaFalhandoLancaAssistenteIndisponivelComAsFalhasRegistradas() {
        provedores.com(provedor(1L, "a", 1, true), provedor(2L, "b", 2, true));
        AssistenteFalso a = new AssistenteFalso("a")
                .falharCom(new FalhaNaChamadaDeIa(ResultadoDaChamada.ERRO_DE_REDE, null, "sem rede"));
        AssistenteFalso b = new AssistenteFalso("b")
                .falharCom(new FalhaNaChamadaDeIa(ResultadoDaChamada.TEMPO_ESGOTADO, null, "demorou"));

        assertThatThrownBy(() -> cadeia(a, b).recomendar(PERFIL, CANDIDATOS))
                .isInstanceOf(AssistenteIndisponivel.class)
                .hasMessage(AssistenteIndisponivel.MENSAGEM);

        assertThat(chamadas.todas()).extracting(ChamadaIa::tentativa).containsExactly(1, 2);
        assertThat(chamadas.todas()).extracting(ChamadaIa::resultado)
                .containsExactly(ResultadoDaChamada.ERRO_DE_REDE, ResultadoDaChamada.TEMPO_ESGOTADO);
    }

    @Test
    void rnfRec001_fakeTambemERegistrado() {
        provedores.com(provedor(4L, "fake", 4, true));

        Atendimento<RespostaDeRecomendacao> atendimento =
                cadeia(new AssistenteDeLivrosFake()).recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("fake");
        assertThat(atendimento.resposta().itens()).extracting(item -> item.livroId()).containsExactly(1L);
        assertThat(chamadas.todas()).hasSize(1);
        ChamadaIa chamada = chamadas.todas().get(0);
        assertThat(chamada.provedorIaId()).isEqualTo(4L);
        assertThat(chamada.modelo()).isEqualTo("fake");
        assertThat(chamada.resultado()).isEqualTo(ResultadoDaChamada.SUCESSO);
        assertThat(chamada.tentativa()).isEqualTo(1);
        assertThat(chamada.tokensEntrada()).isNull();
        assertThat(chamada.tokensSaida()).isNull();
    }

    @Test
    void rnfRec001_conversarRegistraAOperacaoConversar() {
        provedores.com(provedor(1L, "a", 1, true));
        AssistenteFalso a = new AssistenteFalso("a");

        Atendimento<RespostaDoAssistente> atendimento =
                cadeia(a).conversar(PERFIL, List.of(), "Olá", FERRAMENTAS);

        assertThat(atendimento.provedor()).isEqualTo("a");
        assertThat(atendimento.resposta().texto()).isEqualTo("resposta de a");
        assertThat(chamadas.todas()).hasSize(1);
        assertThat(chamadas.todas().get(0).operacao()).isEqualTo(OperacaoIa.CONVERSAR);
    }

    private CadeiaDeProvedores cadeia(AssistenteDeLivros... assistentes) {
        return new CadeiaDeProvedores(List.of(assistentes), provedores, chamadas, relogio);
    }

    private static ProvedorIa provedor(long id, String nome, int ordem, boolean ativo) {
        return new ProvedorIa(id, nome, ordem, ativo);
    }

    /** Relógio ajustável nos testes, sem depender do tempo real (DEC-03). */
    private static final class RelogioDeTeste implements br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio {

        private LocalDateTime agora = LocalDateTime.of(2026, 10, 25, 10, 0);

        @Override
        public LocalDateTime agora() {
            return agora;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }
    }

    private static final class ProvedoresEmMemoria implements ProvedoresIaRepositorio {

        private final List<ProvedorIa> provedores = new ArrayList<>();

        void com(ProvedorIa... novos) {
            provedores.clear();
            provedores.addAll(List.of(novos));
        }

        @Override
        public List<ProvedorIa> listarEmOrdem() {
            return provedores.stream().sorted(Comparator.comparingInt(ProvedorIa::ordem)).toList();
        }

        @Override
        public List<ProvedorIa> salvar(List<ProvedorIa> novos) {
            com(novos.toArray(ProvedorIa[]::new));
            return List.copyOf(provedores);
        }
    }

    private static final class ChamadasEmMemoria implements ChamadasIaRepositorio {

        private final List<ChamadaIa> chamadas = new ArrayList<>();
        private long proximoId = 1;

        @Override
        public Long registrar(ChamadaIa chamada) {
            chamadas.add(chamada);
            return proximoId++;
        }

        List<ChamadaIa> todas() {
            return List.copyOf(chamadas);
        }
    }

    /** Assistente configurável: falha as próximas chamadas na ordem em que forem programadas. */
    private static final class AssistenteFalso implements AssistenteDeLivros {

        private final String nome;
        private final String modelo;
        private final boolean temChave;
        private final Deque<RuntimeException> falhas = new ArrayDeque<>();
        private RespostaDoProvedor<RespostaDeRecomendacao> respostaDeRecomendacao =
                RespostaDoProvedor.semTokens(new RespostaDeRecomendacao(List.of()));
        private RespostaDoProvedor<RespostaDoAssistente> respostaDoAssistente = RespostaDoProvedor.semTokens(
                new RespostaDoAssistente(TipoDeResposta.DUVIDA, "resposta de a", List.of()));

        AssistenteFalso(String nome) {
            this(nome, true);
        }

        AssistenteFalso(String nome, boolean temChave) {
            this.nome = nome;
            this.modelo = "modelo-" + nome;
            this.temChave = temChave;
        }

        AssistenteFalso falharCom(RuntimeException falha) {
            falhas.add(falha);
            return this;
        }

        @Override
        public String nome() {
            return nome;
        }

        @Override
        public String modelo() {
            return modelo;
        }

        @Override
        public boolean temChave() {
            return temChave;
        }

        @Override
        public RespostaDoProvedor<RespostaDeRecomendacao> recomendar(
                PerfilDoCliente perfil, List<LivroCandidato> candidatos) {
            falharSeProgramado();
            return respostaDeRecomendacao;
        }

        @Override
        public RespostaDoProvedor<RespostaDoAssistente> conversar(
                PerfilDoCliente perfil,
                List<MensagemAnterior> historico,
                String mensagemNova,
                FerramentasDoAssistente ferramentas) {
            falharSeProgramado();
            return respostaDoAssistente;
        }

        private void falharSeProgramado() {
            RuntimeException falha = falhas.poll();
            if (falha != null) {
                throw falha;
            }
        }
    }

    private static final class FerramentasVazias implements FerramentasDoAssistente {

        @Override
        public List<ResumoDeLivro> buscarLivros(String texto, String categoria, BigDecimal precoMinimo,
                BigDecimal precoMaximo) {
            return List.of();
        }

        @Override
        public Optional<DetalheDoLivro> verLivro(Long livroId) {
            return Optional.empty();
        }

        @Override
        public List<LivroDoHistorico> verMeuHistorico() {
            return List.of();
        }

        @Override
        public List<ResumoDeLivro> recomendarParaMim() {
            return List.of();
        }
    }
}
