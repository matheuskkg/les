package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ResolverEnderecoDaCompra;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.ConferirCartoes;
import br.fatec.les.livraria.vendas.aplicacao.ConferirCupons;
import br.fatec.les.livraria.vendas.aplicacao.CotarFrete;
import br.fatec.les.livraria.vendas.aplicacao.MontarCheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CartaoNoPagamento;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PagamentoConferido;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Confere a forma de pagamento do checkout aberto antes da finalização (RF-VEN-006, RF-VEN-007, RN-VEN-003 a
 * RN-VEN-007, DEC-11): frete pelo endereço de entrega, cupons e distribuição. Os cartões só são conferidos se
 * informados, para que a tela mostre o restante antes de o cliente escolhê-los. Só lê: nada fica gravado no checkout
 * (DEC-16), e o código de segurança não é recebido aqui.
 */
@Component
@RequiredArgsConstructor
public class ConferirPagamento {

    private final IdentificarCliente identificarCliente;
    private final CheckoutRepositorio checkouts;
    private final ResolverEnderecoDaCompra resolverEndereco;
    private final CotarFrete cotarFrete;
    private final MontarCheckoutConsultado montarCheckout;
    private final ConferirCupons conferirCupons;
    private final ConferirCartoes conferirCartoes;
    private final Relogio relogio;

    /**
     * @throws ExcecaoDeRecursoNaoEncontrado se não houver checkout aberto ou se o endereço, um cupom de troca ou um
     *     cartão não forem do cliente
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações do pagamento
     */
    @Transactional(readOnly = true)
    public PagamentoConferido executar(PagamentoInformado pagamento) {
        Long clienteId = identificarCliente.executar();
        Checkout checkout =
                checkouts
                        .buscarAbertoPorCliente(clienteId)
                        .filter(aberto -> !aberto.venceuEm(relogio.agora()))
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Nenhuma compra em andamento."));
        Endereco entrega = resolverEndereco.executar(pagamento.entrega());

        Dinheiro subtotal = montarCheckout.montar(checkout).subtotal();
        Dinheiro frete = cotarFrete.cotar(entrega.cep(), checkout.getItens());
        DistribuicaoDoPagamento distribuicao =
                conferirCupons.conferir(
                        clienteId, subtotal, frete, pagamento.cuponsPromocionais(), pagamento.cuponsDeTroca());

        List<CartaoNoPagamento> cartoes =
                pagamento.cartoes().isEmpty() ? List.of() : conferirCartoes.conferir(distribuicao, pagamento.cartoes());
        return new PagamentoConferido(distribuicao, cartoes);
    }
}
