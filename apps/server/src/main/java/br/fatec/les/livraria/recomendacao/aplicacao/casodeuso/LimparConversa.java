package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import lombok.RequiredArgsConstructor;

/**
 * "Limpar conversa" (RNF-REC-001, DEC-03): arquiva a conversa ativa do cliente, que fica no banco e não é exibida a
 * ninguém; a próxima mensagem abre outra. Sem conversa ativa, não faz nada.
 */
@Component
@RequiredArgsConstructor
public class LimparConversa {

    private final IdentificarCliente identificarCliente;
    private final ConversasRepositorio conversas;
    private final Relogio relogio;

    public void executar() {
        Long clienteId = identificarCliente.executar();
        conversas.conversaAtiva(clienteId).ifPresent(conversaId -> conversas.arquivar(conversaId, relogio.agora()));
    }
}
