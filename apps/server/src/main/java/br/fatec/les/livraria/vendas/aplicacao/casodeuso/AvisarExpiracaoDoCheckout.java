package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarUsuarioDoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.Notificar;
import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import lombok.RequiredArgsConstructor;

/**
 * Avisa o cliente de que o prazo do checkout está perto de vencer (RN-VEN-014, DEC-12, DEC-13), com a antecedência de
 * {@code livraria.checkout.aviso-expiracao}. Trava o checkout e confere de novo antes de avisar.
 */
@Component
@RequiredArgsConstructor
public class AvisarExpiracaoDoCheckout {

    static final String MENSAGEM = "O prazo para pagar a sua compra está perto de acabar. Finalize o pagamento para não "
            + "perder a reserva dos itens.";

    private final CheckoutRepositorio checkouts;
    private final PropriedadesDoCheckout propriedades;
    private final Relogio relogio;
    private final ConsultarUsuarioDoCliente consultarUsuario;
    private final Notificar notificar;

    /** @param checkoutId checkout cujo prazo está perto de vencer */
    @Transactional
    public void executar(Long checkoutId) {
        LocalDateTime agora = relogio.agora();
        checkouts.buscarParaAlterar(checkoutId)
                .filter(checkout -> checkout.deveAvisarEm(agora, propriedades.avisoExpiracao()))
                .ifPresent(checkout -> avisar(checkout, agora));
    }

    private void avisar(Checkout checkout, LocalDateTime agora) {
        checkout.registrarAviso(agora);
        checkouts.salvar(checkout);
        consultarUsuario.executar(checkout.getClienteId()).ifPresent(usuarioId -> notificar.executar(
                Notificar.Comando.sobre(usuarioId, TipoNotificacao.CHECKOUT_EXPIRANDO, MENSAGEM, "checkout",
                        checkout.getId())));
    }
}
