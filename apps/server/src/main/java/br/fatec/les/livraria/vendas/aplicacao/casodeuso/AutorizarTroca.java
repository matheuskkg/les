package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.DetalharPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocaRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.troca.Troca;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * O administrador autoriza uma troca: {@code EM TROCA} → {@code TROCA AUTORIZADA} (RF-VEN-012, DEC-12). O pedido
 * continua {@code ENTREGUE}, e o cliente recebe {@code TROCA_AUTORIZADA} (RN-VEN-016, DEC-13). A troca é travada até o
 * fim da transação; autorizar ou recusar de novo responde 409.
 */
@Component
@RequiredArgsConstructor
public class AutorizarTroca {

    private final TrocaRepositorio trocas;
    private final PedidosConsultas pedidos;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;
    private final DetalharPedido detalharPedido;

    /**
     * @param id número da troca
     * @return o pedido da troca, com as trocas atualizadas
     * @throws ExcecaoDeRecursoNaoEncontrado se a troca não existir
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito se a troca não estiver {@code EM TROCA}
     */
    @Transactional
    public PedidoDetalhado executar(Long id) {
        Objects.requireNonNull(id, "id");
        Troca troca =
                trocas.buscarParaAlterar(id)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Troca não encontrada."));
        troca.autorizar();
        trocas.salvar(troca);
        Pedido pedido = pedidos.buscar(troca.getPedidoId()).orElseThrow();
        consultarUsuario
                .executar(pedido.getClienteId())
                .ifPresent(usuarioId ->
                        notificar.executar(
                                Notificar.Comando.sobre(
                                        usuarioId,
                                        TipoNotificacao.TROCA_AUTORIZADA,
                                        "A troca nº %d do pedido nº %d foi autorizada. Envie os itens para a loja."
                                                .formatted(id, pedido.getId()),
                                        "pedido",
                                        pedido.getId())));
        return detalharPedido.executar(pedido);
    }
}
