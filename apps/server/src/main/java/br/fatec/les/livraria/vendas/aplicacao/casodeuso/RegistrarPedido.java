package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ResolverEnderecoDaCompra;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarReservasAtivas;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.aplicacao.ConferirCartoes;
import br.fatec.les.livraria.vendas.aplicacao.ConferirCupons;
import br.fatec.les.livraria.vendas.aplicacao.CopiarClienteDoPedido;
import br.fatec.les.livraria.vendas.aplicacao.CopiarItensDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.CopiasDoPedido;
import br.fatec.les.livraria.vendas.aplicacao.CotarFrete;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CartaoNoPagamento;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsPromocionais;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.pagamento.CobrancaNoCartao;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;
import br.fatec.les.livraria.vendas.dominio.pagamento.ReservasDoCheckout;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.PagamentoCartao;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.regra.CodigosDeSeguranca;
import br.fatec.les.livraria.vendas.dominio.regra.ItensAindaReservados;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Primeira transação da finalização (RF-VEN-008, RN-VEN-002, RN-VEN-007, DEC-11, DEC-13, DEC-15.2): confere o checkout,
 * as reservas e o pagamento, grava o pedido {@code EM PROCESSAMENTO} com as cópias e consome os cupons.
 *
 * <p>Trava o checkout antes de tudo: uma segunda finalização do mesmo checkout espera o commit desta e encontra o
 * pedido em processamento (409). O cupom de troca usado por outro pedido falha na versão, e o promocional já usado pelo
 * cliente, no {@code UNIQUE}; os dois também respondem 409 e desfazem o pedido.
 */
@Component
@RequiredArgsConstructor
public class RegistrarPedido {

    static final String SEM_CHECKOUT = "Nenhuma compra em andamento.";

    private final IdentificarCliente identificarCliente;
    private final CheckoutRepositorio checkouts;
    private final PedidoRepositorio pedidos;
    private final ConsultarReservasAtivas consultarReservas;
    private final ResolverEnderecoDaCompra resolverEndereco;
    private final CopiarItensDoCheckout copiarItens;
    private final CopiarClienteDoPedido copiarCliente;
    private final CotarFrete cotarFrete;
    private final ConferirCupons conferirCupons;
    private final ConferirCartoes conferirCartoes;
    private final CuponsPromocionais cuponsPromocionais;
    private final CuponsDeTroca cuponsDeTroca;
    private final CodigosDeSeguranca codigosDeSeguranca; // RN-CLI-004
    private final ItensAindaReservados itensAindaReservados; // RN-VEN-002
    private final Relogio relogio;

    /**
     * @throws ExcecaoDeRecursoNaoEncontrado se não houver checkout aberto, ou se um endereço, cupom de troca ou cartão
     *     não for do cliente
     * @throws ExcecaoDeConflito se o checkout já tiver um pedido em processamento
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações das reservas, dos itens ou
     *     do pagamento
     */
    @Transactional
    public PedidoRegistrado executar(FinalizacaoInformada finalizacao) {
        Long clienteId = identificarCliente.executar();
        LocalDateTime agora = relogio.agora();
        Checkout checkout =
                checkouts
                        .buscarAbertoPorClienteParaAlterar(clienteId)
                        .filter(aberto -> !aberto.venceuEm(agora))
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado(SEM_CHECKOUT));
        if (pedidos.buscarEmProcessamentoDoCheckout(checkout.getId()).isPresent()) {
            throw new ExcecaoDeConflito("O pagamento desta compra já está sendo processado.");
        }
        Violacoes.verificar(finalizacao.codigosDeSeguranca(), codigosDeSeguranca);
        Violacoes.verificar(
                new ReservasDoCheckout(
                        checkout.getItens(), consultarReservas.executar(OrigemReserva.CHECKOUT, checkout.getId())),
                itensAindaReservados);

        PagamentoInformado pagamento = finalizacao.pagamento();
        List<ItemPedido> itens = copiarItens.executar(checkout.getItens());
        Endereco entrega = resolverEndereco.executar(pagamento.entrega());
        Endereco cobranca = resolverEndereco.executar(finalizacao.cobranca());
        Dinheiro subtotal = itens.stream().map(ItemPedido::subtotal).reduce(Dinheiro.ZERO, Dinheiro::somar);
        Dinheiro frete = cotarFrete.cotar(entrega.cep(), checkout.getItens());
        DistribuicaoDoPagamento distribuicao =
                conferirCupons.conferir(
                        clienteId, subtotal, frete, pagamento.cuponsPromocionais(), pagamento.cuponsDeTroca());
        List<CartaoNoPagamento> cartoes = conferirCartoes.conferir(distribuicao, pagamento.cartoes());

        Pedido pedido =
                pedidos.salvar(
                        Pedido.registrar(
                                checkout.getId(),
                                clienteId,
                                agora,
                                copiarCliente.executar(clienteId),
                                CopiasDoPedido.endereco(entrega),
                                CopiasDoPedido.endereco(cobranca),
                                itens,
                                cartoes.stream()
                                        .map(cartao ->
                                                new PagamentoCartao(
                                                        CopiasDoPedido.cartao(cartao.cartao()), cartao.valor()))
                                        .toList(),
                                CopiasDoPedido.cupons(distribuicao),
                                frete,
                                distribuicao.descontoPromocional()));
        if (distribuicao.promocional() != null) {
            cuponsPromocionais.registrarUso(distribuicao.promocional().id(), clienteId, pedido.getId());
        }
        List<Long> trocas = pedido.cuponsDeTrocaUsados();
        if (!trocas.isEmpty()) {
            cuponsDeTroca.marcarComoUsados(trocas);
        }
        return new PedidoRegistrado(pedido, cobrancas(cartoes, finalizacao.codigosDeSeguranca()), distribuicao.troco());
    }

    private static List<CobrancaNoCartao> cobrancas(List<CartaoNoPagamento> cartoes, List<String> codigos) {
        List<CobrancaNoCartao> cobrancas = new ArrayList<>();
        for (int i = 0; i < cartoes.size(); i++) {
            CartaoNoPagamento cartao = cartoes.get(i);
            cobrancas.add(
                    new CobrancaNoCartao(
                            cartao.cartao().numero(), cartao.cartao().validade(), codigos.get(i), cartao.valor()));
        }
        return cobrancas;
    }
}
