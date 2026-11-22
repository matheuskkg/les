package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.aplicacao.AssistenteIndisponivel;
import br.fatec.les.livraria.recomendacao.aplicacao.FerramentasDoCatalogo;
import br.fatec.les.livraria.recomendacao.aplicacao.LerCatalogo;
import br.fatec.les.livraria.recomendacao.aplicacao.MontarPerfilDoCliente;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.MensagemExibida;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RecomendacoesRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.regra.LivroAVenda;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ReenvioSemResposta;
import br.fatec.les.livraria.recomendacao.dominio.calculo.RemoverLivrosCompradosDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ValidarRespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.EnvioDeMensagem;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.regra.LimiteDeMensagensPorMinuto;
import br.fatec.les.livraria.recomendacao.dominio.regra.TamanhoDaMensagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarComprasDoCliente;
import lombok.RequiredArgsConstructor;

/**
 * Envia a mensagem do cliente ao assistente e grava a resposta (RNF-REC-001, DEC-03).
 *
 * <ol>
 *   <li>Antes da chamada: {@link TamanhoDaMensagem} e {@link LimiteDeMensagensPorMinuto}; violação responde 422.</li>
 *   <li>O histórico enviado à IA são as {@value #HISTORICO} últimas mensagens da conversa ativa, lidas do banco antes
 *       de gravar a nova; sem conversa ativa, uma nova é aberta.</li>
 *   <li>A mensagem do cliente é gravada antes da chamada. Se toda a cadeia falhar, ela fica gravada e a API responde
 *       503; o reenvio do mesmo texto não a grava de novo ({@link ReenvioSemResposta}).</li>
 *   <li>A resposta passa por {@link ValidarRespostaDoAssistente} e, quando é uma recomendação, por
 *       {@link RemoverLivrosCompradosDaRecomendacao}; depois é gravada apontando para a chamada que a gerou.</li>
 * </ol>
 *
 * <p>A checagem do limite, a abertura da conversa e a gravação da mensagem do cliente ficam sob uma trava por
 * cliente, em memória (a aplicação roda em uma instância só); a chamada à IA fica fora dela.</p>
 *
 * <p>Não há transação em volta do caso de uso: cada gravação usa a do repositório, e nenhuma fica aberta durante a
 * chamada à IA, que pode levar segundos.</p>
 */
@Component
@RequiredArgsConstructor
public class EnviarMensagemAoAssistente {

    static final int HISTORICO = 10;

    private final IdentificarCliente identificarCliente;
    private final ConversasRepositorio conversas;
    private final RecomendacoesRepositorio recomendacoes;
    private final ConsultarComprasDoCliente consultarComprasDoCliente;
    private final LerCatalogo lerCatalogo;
    private final MontarPerfilDoCliente montarPerfil;
    private final FerramentasDoCatalogo ferramentas;
    private final CadeiaDeProvedores cadeia;
    private final ValidarRespostaDoAssistente validarResposta;
    private final RemoverLivrosCompradosDaRecomendacao removerComprados;
    private final ReenvioSemResposta reenvioSemResposta;
    private final TamanhoDaMensagem tamanhoDaMensagem;
    private final LimiteDeMensagensPorMinuto limiteDeMensagens;
    private final Relogio relogio;
    private final ConcurrentMap<Long, Object> travas = new ConcurrentHashMap<>();

    /**
     * @param texto mensagem digitada pelo cliente
     * @return a resposta gravada do assistente, com os livros citados
     * @throws AssistenteIndisponivel se nenhum provedor atender; a mensagem do cliente fica gravada
     */
    public MensagemExibida executar(String texto) {
        Long clienteId = identificarCliente.executar();
        String mensagem;
        Long conversaId;
        List<MensagemAnterior> historico;
        // Por cliente: duas mensagens simultâneas não abrem duas conversas ativas nem passam juntas pelo limite.
        synchronized (travas.computeIfAbsent(clienteId, id -> new Object())) {
            LocalDateTime agora = relogio.agora();
            Violacoes.verificar(
                    new EnvioDeMensagem(texto, conversas.enviadasPeloClienteDesde(clienteId, agora.minusMinutes(1))),
                    tamanhoDaMensagem, limiteDeMensagens);
            mensagem = texto.strip();
            conversaId = conversas.conversaAtiva(clienteId).orElseGet(() -> conversas.abrir(clienteId, agora));
            List<MensagemDaConversa> ultimas = conversas.ultimas(conversaId, HISTORICO + 1);
            if (reenvioSemResposta.reenvio(ultimas, mensagem)) {
                // A mensagem já está gravada desde a tentativa que falhou: sai do histórico e não é gravada de novo.
                ultimas = ultimas.subList(0, ultimas.size() - 1);
            } else {
                conversas.salvar(MensagemDaConversa.doCliente(conversaId, mensagem, agora));
            }
            historico = ultimas.stream()
                    .skip(Math.max(0, ultimas.size() - HISTORICO))
                    .map(MensagemDaConversa::comoHistorico)
                    .toList();
        }

        PerfilDoCliente perfil = montarPerfil.montar(consultarComprasDoCliente.executar(clienteId),
                recomendacoes.avaliacoesDoCliente(clienteId));
        List<LivroListado> ativos = lerCatalogo.ativos();
        List<ResumoDeLivro> catalogo = ativos.stream()
                .filter(LivroAVenda::aVenda)
                .map(ResumoDeLivro::de)
                .toList();
        Atendimento<RespostaDoAssistente> atendimento = cadeia.conversar(perfil, historico, mensagem,
                ferramentas.doCliente(clienteId), catalogo);

        Map<Long, LivroListado> aVenda = ativos.stream()
                .filter(LivroAVenda::aVenda)
                .collect(Collectors.toMap(livro -> livro.livro().id(), Function.identity()));
        RespostaDoAssistente resposta = validarResposta.validar(atendimento.resposta(), aVenda.keySet());
        resposta = removerComprados.remover(resposta, perfil.livrosComprados().stream()
                .map(LivroComprado::id)
                .toList());
        MensagemDaConversa gravada = conversas.salvar(MensagemDaConversa.doAssistente(conversaId, resposta,
                atendimento.chamadaIaId(), relogio.agora()));
        return new MensagemExibida(gravada, resposta.livrosCitados().stream().map(aVenda::get).toList());
    }
}
