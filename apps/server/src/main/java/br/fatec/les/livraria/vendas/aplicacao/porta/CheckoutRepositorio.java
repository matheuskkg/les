package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Persistência da sessão de compra e dos seus itens (RF-VEN-003, DEC-12, DEC-16). */
public interface CheckoutRepositorio {

    /** Checkout aberto do cliente; há no máximo um (DEC-12). */
    Optional<Checkout> buscarAbertoPorCliente(Long clienteId);

    /** Como {@link #buscarAbertoPorCliente(Long)}, travando o checkout até o fim da transação. */
    Optional<Checkout> buscarAbertoPorClienteParaAlterar(Long clienteId);

    /** Cliente dono do checkout, sem travar, para travar o carrinho antes do checkout. */
    Optional<Long> buscarClienteId(Long checkoutId);

    /** Checkout pelo id, travado até o fim da transação, para a expiração (RN-VEN-014). */
    Optional<Checkout> buscarParaAlterar(Long checkoutId);

    /** Checkouts abertos e sem aviso cujo prazo vence em {@code (agora, limite]} (RN-VEN-014). */
    List<Long> listarIdsParaAvisar(LocalDateTime agora, LocalDateTime limite);

    /** Checkouts abertos com prazo vencido até {@code agora} (RN-VEN-014). */
    List<Long> listarIdsVencidos(LocalDateTime agora);

    /**
     * Grava o checkout, criando o id e os itens na primeira gravação.
     *
     * @throws org.springframework.dao.OptimisticLockingFailureException se outra transação o alterou antes (DEC-13)
     */
    Checkout salvar(Checkout checkout);
}
