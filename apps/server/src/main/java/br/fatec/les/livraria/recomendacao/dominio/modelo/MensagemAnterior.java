package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Mensagem já trocada na conversa ativa, enviada como histórico à IA (RNF-REC-001, DEC-03). O histórico é lido do
 * banco, nunca recebido na requisição do cliente.
 */
public record MensagemAnterior(
        PapelDaMensagem papel,
        String texto) {
}
