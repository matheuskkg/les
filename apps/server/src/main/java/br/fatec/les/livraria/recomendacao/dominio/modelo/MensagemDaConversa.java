package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Mensagem da conversa do cliente com o assistente (RNF-REC-001, DEC-03). As do cliente não têm tipo, livros nem
 * chamada; as do assistente guardam o tipo da resposta, os livros citados e a chamada que as gerou.
 *
 * @param id            id da mensagem, nulo antes de gravar
 * @param conversaId    conversa da mensagem
 * @param papel         quem escreveu
 * @param texto         texto exibido
 * @param tipo          tipo da resposta do assistente, ou nulo na mensagem do cliente
 * @param livrosCitados ids dos livros citados pelo assistente, na ordem da resposta
 * @param chamadaIaId   chamada que gerou a resposta, ou nula
 * @param enviadaEm     quando foi gravada, pelo relógio da aplicação
 * @param avaliacao     última avaliação do cliente à resposta, ou nula
 * @param avaliadaEm    quando a última avaliação foi dada, ou nulo
 */
public record MensagemDaConversa(
        @Nullable Long id,
        Long conversaId,
        PapelDaMensagem papel,
        String texto,
        @Nullable TipoDeResposta tipo,
        List<Long> livrosCitados,
        @Nullable Long chamadaIaId,
        LocalDateTime enviadaEm,
        @Nullable Avaliacao avaliacao,
        @Nullable LocalDateTime avaliadaEm) {

    public MensagemDaConversa {
        Objects.requireNonNull(conversaId, "conversaId");
        Objects.requireNonNull(papel, "papel");
        Objects.requireNonNull(texto, "texto");
        Objects.requireNonNull(enviadaEm, "enviadaEm");
        livrosCitados = List.copyOf(livrosCitados);
    }

    /** Mensagem nova do cliente. */
    public static MensagemDaConversa doCliente(Long conversaId, String texto, LocalDateTime em) {
        return new MensagemDaConversa(null, conversaId, PapelDaMensagem.CLIENTE, texto, null, List.of(), null, em,
                null, null);
    }

    /** Resposta nova do assistente, já validada pelo server. */
    public static MensagemDaConversa doAssistente(Long conversaId, RespostaDoAssistente resposta,
            @Nullable Long chamadaIaId, LocalDateTime em) {
        return new MensagemDaConversa(null, conversaId, PapelDaMensagem.ASSISTENTE, resposta.texto(),
                resposta.tipo(), resposta.livrosCitados(), chamadaIaId, em, null, null);
    }

    /** Troca a avaliação pela nova; a última vale (DEC-03). */
    public MensagemDaConversa avaliar(Avaliacao nova, LocalDateTime em) {
        return new MensagemDaConversa(id, conversaId, papel, texto, tipo, livrosCitados, chamadaIaId, enviadaEm,
                Objects.requireNonNull(nova, "nova"), Objects.requireNonNull(em, "em"));
    }

    /** Como a mensagem vai no histórico enviado à IA. */
    public MensagemAnterior comoHistorico() {
        return new MensagemAnterior(papel, texto);
    }
}
