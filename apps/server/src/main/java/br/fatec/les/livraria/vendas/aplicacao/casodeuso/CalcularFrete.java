package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ResolverEnderecoDaCompra;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.CotarFrete;
import br.fatec.les.livraria.vendas.aplicacao.MontarCheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.FreteCalculado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcula o frete do checkout aberto para o endereço de entrega escolhido (RF-VEN-004, RF-VEN-005, DEC-05.3): um dos
 * endereços do cliente ou um novo. Só lê: nem o endereço nem o frete ficam gravados no checkout (DEC-16).
 */
@Component
@RequiredArgsConstructor
public class CalcularFrete {

    private final IdentificarCliente identificarCliente;
    private final CheckoutRepositorio checkouts;
    private final ResolverEnderecoDaCompra resolverEndereco;
    private final CotarFrete cotarFrete;
    private final MontarCheckoutConsultado montarCheckout;
    private final Relogio relogio;

    /**
     * @throws ExcecaoDeRecursoNaoEncontrado se não houver checkout aberto ou o endereço não for do cliente
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se a escolha ou o endereço novo forem
     *     inválidos
     */
    @Transactional(readOnly = true)
    public FreteCalculado executar(EscolhaDeEndereco entrega) {
        Long clienteId = identificarCliente.executar();
        Checkout checkout =
                checkouts
                        .buscarAbertoPorCliente(clienteId)
                        .filter(aberto -> !aberto.venceuEm(relogio.agora()))
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Nenhuma compra em andamento."));
        Endereco endereco = resolverEndereco.executar(entrega);

        Dinheiro frete = cotarFrete.cotar(endereco.cep(), checkout.getItens());
        Dinheiro subtotal = montarCheckout.montar(checkout).subtotal();
        return new FreteCalculado(frete, subtotal, subtotal.somar(frete));
    }
}
