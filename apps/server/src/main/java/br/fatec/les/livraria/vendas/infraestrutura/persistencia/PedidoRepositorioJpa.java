package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.CupomPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.PagamentoCartao;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA do pedido (RF-VEN-008, DEC-12, DEC-16). As cópias são gravadas na criação e não mudam: depois dela, só o
 * status e a data da entrega são alterados.
 */
@Component
@RequiredArgsConstructor
public class PedidoRepositorioJpa implements PedidoRepositorio {

    private final PedidoJpaRepository repositorio;
    private final CheckoutJpaRepository checkouts;

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        PedidoJpa entidade = pedido.getId() == null ? nova(pedido)
                : repositorio.findById(pedido.getId()).orElseThrow(
                        () -> new IllegalStateException("Pedido não encontrado: " + pedido.getId()));
        entidade.setStatus(pedido.getStatus());
        entidade.setEntregueEm(pedido.getEntregueEm());
        return paraDominio(repositorio.saveAndFlush(entidade));
    }

    @Override
    @Transactional
    public Optional<Pedido> buscarParaAlterar(Long pedidoId) {
        return repositorio.buscarPorIdComLock(pedidoId).map(PedidoRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional
    public Optional<Pedido> buscarEmProcessamentoDoCheckout(Long checkoutId) {
        return repositorio.buscarPorCheckoutComLock(checkoutId, StatusPedido.EM_PROCESSAMENTO)
                .map(PedidoRepositorioJpa::paraDominio);
    }

    private PedidoJpa nova(Pedido pedido) {
        PedidoJpa entidade = new PedidoJpa();
        if (pedido.getCheckoutId() != null) {
            entidade.setCheckout(checkouts.getReferenceById(pedido.getCheckoutId()));
        }
        entidade.setClienteId(pedido.getClienteId());
        entidade.setRealizadoEm(pedido.getRealizadoEm());
        entidade.setCliente(pedido.getCliente());
        entidade.setEnderecoEntrega(pedido.getEnderecoEntrega());
        entidade.setEnderecoCobranca(pedido.getEnderecoCobranca());
        entidade.setSubtotalItens(pedido.getSubtotal().valor());
        entidade.setFrete(pedido.getFrete().valor());
        entidade.setDescontoPromocional(pedido.getDescontoPromocional().valor());
        entidade.setTotal(pedido.getTotal().valor());
        for (ItemPedido item : pedido.getItens()) {
            ItemPedidoJpa linha = new ItemPedidoJpa();
            linha.setPedido(entidade);
            linha.setLivro(item.livro());
            linha.setPrecoUnitario(item.precoUnitario().valor());
            linha.setQuantidade(item.quantidade());
            entidade.getItens().add(linha);
        }
        for (PagamentoCartao pagamento : pedido.getCartoes()) {
            PagamentoCartaoJpa linha = new PagamentoCartaoJpa();
            linha.setPedido(entidade);
            linha.setCartao(pagamento.cartao());
            linha.setValor(pagamento.valor().valor());
            entidade.getCartoes().add(linha);
        }
        for (CupomPedido cupom : pedido.getCupons()) {
            CupomPedidoJpa linha = new CupomPedidoJpa();
            linha.setPedido(entidade);
            linha.setCupom(cupom.cupom());
            linha.setValorAbatido(cupom.valorAbatido().valor());
            entidade.getCupons().add(linha);
        }
        return entidade;
    }

    private static Pedido paraDominio(PedidoJpa entidade) {
        return new Pedido(entidade.getId(), entidade.getCheckout() == null ? null : entidade.getCheckout().getId(),
                entidade.getClienteId(), entidade.getStatus(), entidade.getRealizadoEm(), entidade.getEntregueEm(),
                entidade.getCliente(), entidade.getEnderecoEntrega(), entidade.getEnderecoCobranca(),
                entidade.getItens().stream()
                        .map(item -> new ItemPedido(item.getId(), item.getLivro(),
                                Dinheiro.de(item.getPrecoUnitario()), item.getQuantidade()))
                        .toList(),
                entidade.getCartoes().stream()
                        .map(cartao -> new PagamentoCartao(cartao.getCartao(), Dinheiro.de(cartao.getValor())))
                        .toList(),
                entidade.getCupons().stream()
                        .map(cupom -> new CupomPedido(cupom.getCupom(), Dinheiro.de(cupom.getValorAbatido())))
                        .toList(),
                Dinheiro.de(entidade.getSubtotalItens()), Dinheiro.de(entidade.getFrete()),
                Dinheiro.de(entidade.getDescontoPromocional()), Dinheiro.de(entidade.getTotal()));
    }
}
