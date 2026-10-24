package br.fatec.les.livraria.vendas.dominio.pedido;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;

/**
 * Pedido criado em cada finalização do checkout (RF-VEN-008, RN-VEN-008, DEC-12). Guarda cópias de tudo o que pode
 * mudar com o tempo (DEC-16) e só aceita as transições do diagrama do DEC-12; as demais respondem 409.
 */
public class Pedido {

    private final Long id;
    private final Long checkoutId;
    private final Long clienteId;
    private final LocalDateTime realizadoEm;
    private final ClienteNoPedido cliente;
    private final EnderecoNoPedido enderecoEntrega;
    private final EnderecoNoPedido enderecoCobranca;
    private final List<ItemPedido> itens;
    private final List<PagamentoCartao> cartoes;
    private final List<CupomPedido> cupons;
    private final Dinheiro subtotal;
    private final Dinheiro frete;
    private final Dinheiro descontoPromocional;
    private final Dinheiro total;
    private StatusPedido status;
    private LocalDateTime entregueEm;

    public Pedido(Long id, Long checkoutId, Long clienteId, StatusPedido status, LocalDateTime realizadoEm,
            LocalDateTime entregueEm, ClienteNoPedido cliente, EnderecoNoPedido enderecoEntrega,
            EnderecoNoPedido enderecoCobranca, List<ItemPedido> itens, List<PagamentoCartao> cartoes,
            List<CupomPedido> cupons, Dinheiro subtotal, Dinheiro frete, Dinheiro descontoPromocional,
            Dinheiro total) {
        this.id = id;
        this.checkoutId = checkoutId;
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.status = Objects.requireNonNull(status, "status");
        this.realizadoEm = Objects.requireNonNull(realizadoEm, "realizadoEm");
        this.entregueEm = entregueEm;
        this.cliente = Objects.requireNonNull(cliente, "cliente");
        this.enderecoEntrega = Objects.requireNonNull(enderecoEntrega, "enderecoEntrega");
        this.enderecoCobranca = Objects.requireNonNull(enderecoCobranca, "enderecoCobranca");
        this.itens = List.copyOf(Objects.requireNonNull(itens, "itens"));
        this.cartoes = List.copyOf(Objects.requireNonNull(cartoes, "cartoes"));
        this.cupons = List.copyOf(Objects.requireNonNull(cupons, "cupons"));
        this.subtotal = Objects.requireNonNull(subtotal, "subtotal");
        this.frete = Objects.requireNonNull(frete, "frete");
        this.descontoPromocional = Objects.requireNonNull(descontoPromocional, "descontoPromocional");
        this.total = Objects.requireNonNull(total, "total");
    }

    /**
     * Cria o pedido {@code EM PROCESSAMENTO} da finalização (RF-VEN-008, DEC-13). O subtotal é a soma dos itens e o
     * total é o subtotal mais o frete, menos o desconto do promocional; o que os cupons de troca e os cartões pagam sai
     * desse total.
     *
     * @throws IllegalArgumentException se não houver itens
     */
    public static Pedido registrar(Long checkoutId, Long clienteId, LocalDateTime realizadoEm,
            ClienteNoPedido cliente, EnderecoNoPedido enderecoEntrega, EnderecoNoPedido enderecoCobranca,
            List<ItemPedido> itens, List<PagamentoCartao> cartoes, List<CupomPedido> cupons, Dinheiro frete,
            Dinheiro descontoPromocional) {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa de ao menos um item.");
        }
        Dinheiro subtotal = itens.stream().map(ItemPedido::subtotal).reduce(Dinheiro.ZERO, Dinheiro::somar);
        Dinheiro total = subtotal.somar(frete).subtrair(descontoPromocional);
        return new Pedido(null, checkoutId, clienteId, StatusPedido.EM_PROCESSAMENTO, realizadoEm, null, cliente,
                enderecoEntrega, enderecoCobranca, itens, cartoes, cupons, subtotal, frete, descontoPromocional,
                total);
    }

    /**
     * A operadora aceitou todos os cartões (RN-VEN-007, DEC-05.2).
     *
     * @throws ExcecaoDeConflito se o pedido não estiver em processamento
     */
    public void aprovar() {
        exigirEmProcessamento();
        status = StatusPedido.APROVADA;
    }

    /**
     * A operadora recusou algum cartão, ou o checkout acabou com o pedido ainda em processamento (RN-VEN-008, DEC-13).
     *
     * @throws ExcecaoDeConflito se o pedido não estiver em processamento
     */
    public void reprovar() {
        exigirEmProcessamento();
        status = StatusPedido.REPROVADA;
    }

    public boolean emProcessamento() {
        return status == StatusPedido.EM_PROCESSAMENTO;
    }

    /** Ids dos cupons de troca usados, para devolvê-los na reprovação (DEC-11, DEC-16). */
    public List<Long> cuponsDeTrocaUsados() {
        return cupons.stream()
                .map(CupomPedido::cupom)
                .filter(cupom -> cupom.categoria() == CategoriaCupom.TROCA)
                .map(CupomNoPedido::id)
                .toList();
    }

    private void exigirEmProcessamento() {
        if (!emProcessamento()) {
            throw new ExcecaoDeConflito("O pedido não está em processamento.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCheckoutId() {
        return checkoutId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public LocalDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public LocalDateTime getEntregueEm() {
        return entregueEm;
    }

    public ClienteNoPedido getCliente() {
        return cliente;
    }

    public EnderecoNoPedido getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public EnderecoNoPedido getEnderecoCobranca() {
        return enderecoCobranca;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public List<PagamentoCartao> getCartoes() {
        return cartoes;
    }

    public List<CupomPedido> getCupons() {
        return cupons;
    }

    public Dinheiro getSubtotal() {
        return subtotal;
    }

    public Dinheiro getFrete() {
        return frete;
    }

    public Dinheiro getDescontoPromocional() {
        return descontoPromocional;
    }

    public Dinheiro getTotal() {
        return total;
    }
}
