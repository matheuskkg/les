package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import lombok.RequiredArgsConstructor;

/**
 * O administrador despacha um pedido aprovado: {@code APROVADA} → {@code EM TRANSPORTE} (RF-VEN-009, RN-VEN-009,
 * DEC-12), e o cliente recebe a notificação {@code PEDIDO_DESPACHADO} (DEC-13). O pedido é travado até o fim da
 * transação, e um segundo despacho responde 409.
 */
@Component
@RequiredArgsConstructor
public class DespacharPedido {

    private final PedidoRepositorio pedidos;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;

    /**
     * @param id número do pedido
     * @return o pedido em transporte
     * @throws ExcecaoDeRecursoNaoEncontrado se o pedido não existir
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito se o pedido não estiver aprovado
     */
    @Transactional
    public Pedido executar(Long id) {
        Objects.requireNonNull(id, "id");
        Pedido pedido = pedidos.buscarParaAlterar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Pedido não encontrado."));
        pedido.despachar();
        Pedido despachado = pedidos.salvar(pedido);
        consultarUsuario.executar(despachado.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.PEDIDO_DESPACHADO,
                        "O pedido nº %d saiu para entrega.".formatted(despachado.getId()), "pedido",
                        despachado.getId())));
        return despachado;
    }
}
