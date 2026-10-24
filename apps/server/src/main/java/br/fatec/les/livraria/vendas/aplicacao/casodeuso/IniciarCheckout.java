package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.TransferirReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.aplicacao.MontarCheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.regra.CarrinhoProntoParaCompra;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;
import lombok.RequiredArgsConstructor;

/**
 * "Ir para pagamento" (RF-VEN-003, DEC-12): leva os itens e as reservas do carrinho para um checkout novo, com prazo
 * {@code livraria.checkout.prazo} contado do início. O carrinho fica vazio e pode receber itens novos.
 *
 * <p>Trava o carrinho primeiro, o que serializa duas tentativas simultâneas do mesmo cliente: a segunda encontra o
 * checkout aberto e recebe 409. Cada reserva passa ao checkout pelo {@link TransferirReserva}, que confere o
 * estoque de novo; se faltar algum item, nada é alterado.</p>
 */
@Component
@RequiredArgsConstructor
public class IniciarCheckout {

    private final IdentificarCliente identificarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final CheckoutRepositorio checkouts;
    private final CarrinhoProntoParaCompra carrinhoPronto;
    private final TransferirReserva transferirReserva;
    private final PropriedadesDoCheckout propriedades;
    private final Relogio relogio;
    private final MontarCheckoutConsultado montarCheckout;

    /**
     * @return o checkout aberto
     * @throws ExcecaoDeConflito se o cliente já tiver um checkout aberto
     * @throws ExcecaoDeNegocio  se o carrinho estiver vazio, tiver livro fora de venda ou faltar estoque
     */
    @Transactional
    public CheckoutConsultado executar() {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho = carrinhos.buscarPorClienteParaAlterar(clienteId)
                .orElseThrow(() -> new ExcecaoDeNegocio("RF-VEN-003", "O carrinho está vazio."));
        if (checkouts.buscarAbertoPorCliente(clienteId).isPresent()) {
            throw new ExcecaoDeConflito("Já existe uma compra em andamento. Volte a ela ou retorne ao carrinho.");
        }
        Violacoes.verificar(carrinho, carrinhoPronto);

        List<ItemCheckout> itens = carrinho.retirarItens().stream()
                .map(item -> new ItemCheckout(item.getLivroId(), item.getQuantidade()))
                .toList();
        Checkout checkout = checkouts.salvar(
                Checkout.iniciar(clienteId, itens, relogio.agora(), propriedades.prazo()));
        for (ItemCheckout item : itens) {
            transferirReserva.executar(item.livroId(), item.quantidade(), OrigemReserva.CARRINHO, carrinho.getId(),
                    OrigemReserva.CHECKOUT, checkout.getId(), checkout.getExpiraEm());
        }
        carrinhos.salvar(carrinho);
        return montarCheckout.montar(checkout);
    }
}
