package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída das conversas com o assistente (RNF-REC-001, DEC-03). Cada operação roda na própria transação: o chat
 * não mantém transação aberta durante a chamada à IA.
 */
public interface ConversasRepositorio {

    /** Conversa ativa do cliente, a não arquivada, se houver. */
    Optional<Long> conversaAtiva(Long clienteId);

    /** Abre uma conversa nova para o cliente e devolve o id. */
    Long abrir(Long clienteId, LocalDateTime em);

    /** Arquiva a conversa; ela fica no banco e não é exibida a ninguém. */
    void arquivar(Long conversaId, LocalDateTime em);

    /** Mensagens da conversa, da mais antiga à mais recente. */
    List<MensagemDaConversa> mensagens(Long conversaId);

    /** As últimas {@code limite} mensagens da conversa, da mais antiga à mais recente. */
    List<MensagemDaConversa> ultimas(Long conversaId, int limite);

    /** Mensagens escritas pelo cliente depois do instante informado, em qualquer conversa dele. */
    long enviadasPeloClienteDesde(Long clienteId, LocalDateTime desde);

    /** Mensagem da conversa ativa do cliente; de outro cliente ou de conversa arquivada não aparece. */
    Optional<MensagemDaConversa> buscarNaConversaAtiva(Long mensagemId, Long clienteId);

    /** Grava a mensagem nova ou a avaliação de uma existente. */
    MensagemDaConversa salvar(MensagemDaConversa mensagem);
}
