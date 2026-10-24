package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsPromocionais;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import lombok.RequiredArgsConstructor;

/**
 * Reprova o pedido em processamento (RN-VEN-007, RN-VEN-008, DEC-11, DEC-12, DEC-13, DEC-15.2): devolve os cupons de
 * troca pelo id da cópia, apaga o uso do promocional e notifica o cliente. O checkout e as reservas continuam.
 *
 * <p>Reprova o pedido recusado pela operadora e também o que ficou em processamento quando o checkout acaba, por
 * expiração, volta ao carrinho ou inativação da conta: sem o número e o código dos cartões, ele não tem mais como ser
 * aprovado.</p>
 */
@Component
@RequiredArgsConstructor
public class ReprovarPedido {

    static final String CHECKOUT_ENCERRADO = "a compra foi encerrada antes da confirmação do pagamento";

    private final CheckoutRepositorio checkouts;
    private final PedidoRepositorio pedidos;
    private final CuponsDeTroca cuponsDeTroca;
    private final CuponsPromocionais cuponsPromocionais;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;

    /**
     * Segunda transação da finalização quando a operadora recusa algum cartão. Trava o checkout e depois o pedido, na
     * mesma ordem da aprovação; se o pedido já saiu de processamento, nada muda.
     *
     * @param motivo motivo da operadora, para a notificação
     */
    @Transactional
    public Pedido executar(Pedido registrado, String motivo) {
        checkouts.buscarParaAlterar(registrado.getCheckoutId());
        Pedido pedido = pedidos.buscarParaAlterar(registrado.getId()).orElseThrow(
                () -> new IllegalStateException("Pedido não encontrado: " + registrado.getId()));
        return pedido.emProcessamento() ? reprovar(pedido, motivo) : pedido;
    }

    /** Reprova o pedido em processamento do checkout que está sendo encerrado, já travado por quem chama. */
    @Transactional
    public void executarPendenteDoCheckout(Long checkoutId) {
        pedidos.buscarEmProcessamentoDoCheckout(checkoutId)
                .ifPresent(pedido -> reprovar(pedido, CHECKOUT_ENCERRADO));
    }

    private Pedido reprovar(Pedido pedido, String motivo) {
        pedido.reprovar();
        Pedido reprovado = pedidos.salvar(pedido);
        List<Long> trocas = reprovado.cuponsDeTrocaUsados();
        if (!trocas.isEmpty()) {
            cuponsDeTroca.devolver(trocas);
        }
        cuponsPromocionais.removerUso(reprovado.getId());
        String mensagem = "O pagamento do pedido nº %d foi reprovado: %s. Os cupons voltaram para você."
                .formatted(reprovado.getId(), motivo);
        consultarUsuario.executar(reprovado.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.PEDIDO_REPROVADO, mensagem, "pedido",
                        reprovado.getId())));
        return reprovado;
    }
}
