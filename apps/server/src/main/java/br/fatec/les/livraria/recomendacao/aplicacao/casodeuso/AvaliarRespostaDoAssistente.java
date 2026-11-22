package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Avaliação positiva ou negativa de uma resposta do assistente; a última vale (RNF-REC-001, DEC-03). Só as respostas do
 * assistente na conversa ativa do cliente podem ser avaliadas; as demais respondem como inexistentes.
 */
@Component
@RequiredArgsConstructor
public class AvaliarRespostaDoAssistente {

    private final IdentificarCliente identificarCliente;
    private final ConversasRepositorio conversas;
    private final Relogio relogio;

    @Transactional
    public void executar(Long mensagemId, Avaliacao avaliacao) {
        Objects.requireNonNull(mensagemId, "mensagemId");
        Objects.requireNonNull(avaliacao, "avaliacao");
        Long clienteId = identificarCliente.executar();
        MensagemDaConversa resposta =
                conversas
                        .buscarNaConversaAtiva(mensagemId, clienteId)
                        .filter(mensagem -> mensagem.papel() == PapelDaMensagem.ASSISTENTE)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Resposta do assistente não encontrada."));
        conversas.salvar(resposta.avaliar(avaliacao, relogio.agora()));
    }
}
