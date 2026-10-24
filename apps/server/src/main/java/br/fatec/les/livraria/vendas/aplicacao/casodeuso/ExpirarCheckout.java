package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.LiberarItensDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import lombok.RequiredArgsConstructor;

/**
 * Expira o checkout cujo prazo venceu (RN-VEN-014, RNF-VEN-001, DEC-12, DEC-13): libera as reservas no
 * {@code estoque}, leva os itens para os removidos do carrinho e notifica o cliente. O pedido que ficou em processamento
 * por falha entre as transações da finalização é reprovado, devolvendo os cupons.
 *
 * <p>Trava o carrinho antes do checkout, na mesma ordem do {@link IniciarCheckout} e do {@link VoltarAoCarrinho}, e
 * confere de novo o vencimento depois de travar.</p>
 */
@Component
@RequiredArgsConstructor
public class ExpirarCheckout {

    static final String MENSAGEM = "O prazo para pagar acabou e os itens da sua compra foram liberados. Eles estão no "
            + "carrinho, entre os removidos, para você adicioná-los de novo.";

    private final CheckoutRepositorio checkouts;
    private final CarrinhoRepositorio carrinhos;
    private final Relogio relogio;
    private final LiberarItensDoCheckout liberarItens;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;
    private final ReprovarPedido reprovarPedido;

    /** @param checkoutId checkout com o prazo vencido */
    @Transactional
    public void executar(Long checkoutId) {
        checkouts.buscarClienteId(checkoutId).ifPresent(clienteId -> {
            Carrinho carrinho = carrinhos.buscarPorClienteParaAlterar(clienteId)
                    .orElseGet(() -> carrinhos.salvar(Carrinho.vazio(clienteId)));
            LocalDateTime agora = relogio.agora();
            checkouts.buscarParaAlterar(checkoutId)
                    .filter(checkout -> checkout.venceuEm(agora))
                    .ifPresent(checkout -> expirar(checkout, carrinho, agora));
        });
    }

    private void expirar(Checkout checkout, Carrinho carrinho, LocalDateTime agora) {
        reprovarPedido.executarPendenteDoCheckout(checkout.getId());
        liberarItens.executar(checkout);
        carrinho.registrarRemovidos(checkout.itensRemovidosEm(agora));
        carrinhos.salvar(carrinho);
        checkout.expirar();
        checkouts.salvar(checkout);
        consultarUsuario.executar(checkout.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.CHECKOUT_EXPIRADO, MENSAGEM, "carrinho",
                        carrinho.getId())));
    }
}
