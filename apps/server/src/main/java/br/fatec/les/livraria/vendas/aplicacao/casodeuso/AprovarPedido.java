package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.BaixarEstoque;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.NotificarAdministradores;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCupomDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import lombok.RequiredArgsConstructor;

/**
 * Segunda transação da finalização quando a operadora aceita todos os cartões (RN-VEN-007, RN-VEN-006, RF-EST-003,
 * DEC-11, DEC-12, DEC-13): o pedido passa a {@code APROVADA}, o estoque é baixado consumindo as reservas, o troco vira
 * cupom de troca, o checkout é concluído e o cliente e os administradores são notificados.
 *
 * <p>Trava o checkout e depois o pedido, na mesma ordem da expiração. Se a expiração já reprovou o pedido, nada muda e
 * o pedido volta como está.</p>
 */
@Component
@RequiredArgsConstructor
public class AprovarPedido {

    private final CheckoutRepositorio checkouts;
    private final PedidoRepositorio pedidos;
    private final BaixarEstoque baixarEstoque;
    private final CuponsDeTroca cuponsDeTroca;
    private final PropriedadesDoCupomDeTroca propriedadesDoCupom;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;
    private final NotificarAdministradores notificarAdministradores;
    private final Relogio relogio;

    /** @param troco excedente dos cupons de troca, calculado na distribuição do pagamento (RN-VEN-006) */
    @Transactional
    public Pedido executar(Pedido registrado, Dinheiro troco) {
        Checkout checkout = checkouts.buscarParaAlterar(registrado.getCheckoutId()).orElseThrow(
                () -> new IllegalStateException("Checkout não encontrado: " + registrado.getCheckoutId()));
        Pedido pedido = pedidos.buscarParaAlterar(registrado.getId()).orElseThrow(
                () -> new IllegalStateException("Pedido não encontrado: " + registrado.getId()));
        if (!pedido.emProcessamento()) {
            return pedido;
        }
        pedido.aprovar();
        Pedido aprovado = pedidos.salvar(pedido);
        baixarEstoque.executar(OrigemReserva.CHECKOUT, checkout.getId(), quantidades(aprovado));
        LocalDateTime agora = relogio.agora();
        if (troco.ehPositivo()) {
            cuponsDeTroca.gerar(new CupomTroca(null, aprovado.getClienteId(), "TROCO-" + aprovado.getId(), troco,
                    propriedadesDoCupom.validadeDeUmCupomGeradoEm(agora.toLocalDate()), SituacaoCupom.DISPONIVEL,
                    agora));
        }
        checkout.concluir();
        checkouts.salvar(checkout);
        notificar(aprovado, troco);
        return aprovado;
    }

    private static Map<Long, Integer> quantidades(Pedido pedido) {
        return pedido.getItens().stream()
                .collect(Collectors.toMap(item -> item.livro().id(), ItemPedido::quantidade, Integer::sum));
    }

    private void notificar(Pedido pedido, Dinheiro troco) {
        String mensagem = "O pagamento do pedido nº %d foi aprovado.".formatted(pedido.getId());
        if (troco.ehPositivo()) {
            mensagem += " O troco de %s virou um cupom de troca.".formatted(troco.emReais());
        }
        String doCliente = mensagem;
        consultarUsuario.executar(pedido.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.PEDIDO_APROVADO, doCliente, "pedido",
                        pedido.getId())));
        notificarAdministradores.executar(NotificarAdministradores.Comando.sobre(
                TipoNotificacao.PEDIDO_AGUARDANDO_DESPACHO,
                "O pedido nº %d foi aprovado e aguarda despacho.".formatted(pedido.getId()), "pedido",
                pedido.getId()));
    }
}
