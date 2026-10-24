package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.DetalharPedido;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PedidoDetalhado;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import lombok.RequiredArgsConstructor;

/**
 * O administrador confirma a entrega: {@code EM TRANSPORTE} → {@code ENTREGUE} (RF-VEN-010, RN-VEN-010, DEC-12). A
 * data da confirmação é gravada como {@code entregueEm}, início do prazo de troca, e o cliente recebe a notificação
 * {@code PEDIDO_ENTREGUE} (DEC-13). O pedido é travado até o fim da transação, e uma segunda confirmação responde 409.
 */
@Component
@RequiredArgsConstructor
public class ConfirmarEntrega {

    private final PedidoRepositorio pedidos;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;
    private final DetalharPedido detalharPedido;
    private final Relogio relogio;

    /**
     * @param id número do pedido
     * @return o pedido entregue
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito se o pedido não estiver em transporte
     */
    @Transactional
    public PedidoDetalhado executar(Long id) {
        Objects.requireNonNull(id, "id");
        Pedido pedido = pedidos.buscarParaAlterar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado."));
        pedido.confirmarEntrega(relogio.agora());
        Pedido entregue = pedidos.salvar(pedido);
        consultarUsuario.executar(entregue.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.PEDIDO_ENTREGUE,
                        "O pedido nº %d foi entregue.".formatted(entregue.getId()), "pedido", entregue.getId())));
        return detalharPedido.executar(entregue);
    }
}
