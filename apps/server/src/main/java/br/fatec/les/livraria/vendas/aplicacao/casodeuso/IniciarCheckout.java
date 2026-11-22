package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * "Ir para pagamento" (RF-VEN-003, DEC-12): leva os itens e as reservas do carrinho para um checkout novo, com prazo
 * {@code livraria.checkout.prazo} contado do início. O carrinho fica vazio e pode receber itens novos.
 *
 * <p>Um checkout aberto com o prazo vencido, que a tarefa ainda não expirou, é expirado aqui. Depois o carrinho é
 * travado, o que serializa duas tentativas simultâneas do mesmo cliente: a segunda encontra o checkout aberto e recebe
 * 409. Cada reserva passa ao checkout pelo {@link TransferirReserva}, em ordem de livro, que confere o estoque de novo;
 * se faltar algum item, nada é alterado.
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
    private final ExpirarCheckout expirarCheckout;

    /**
     * @return o checkout aberto
     * @throws ExcecaoDeConflito se o cliente já tiver um checkout aberto
     * @throws ExcecaoDeNegocio se o carrinho estiver vazio, tiver livro fora de venda ou faltar estoque
     */
    @Transactional
    public CheckoutConsultado executar() {
        Long clienteId = identificarCliente.executar();
        LocalDateTime agora = relogio.agora();
        checkouts
                .buscarAbertoPorCliente(clienteId)
                .ifPresent(aberto -> {
                    if (!aberto.venceuEm(agora)) {
                        throw emAndamento();
                    }
                    expirarCheckout.executar(aberto.getId());
                });
        Carrinho carrinho =
                carrinhos
                        .buscarPorClienteParaAlterar(clienteId)
                        .orElseThrow(() -> new ExcecaoDeNegocio("RF-VEN-003", "O carrinho está vazio."));
        if (checkouts.buscarAbertoPorCliente(clienteId).isPresent()) {
            throw emAndamento();
        }
        Violacoes.verificar(carrinho, carrinhoPronto);

        List<ItemCheckout> itens =
                carrinho.retirarItens().stream()
                        .map(item -> new ItemCheckout(item.getLivroId(), item.getQuantidade()))
                        .toList();
        Checkout checkout = checkouts.salvar(Checkout.iniciar(clienteId, itens, agora, propriedades.prazo()));
        // Livros travados em ordem de id, para que dois checkouts com os mesmos livros não se bloqueiem.
        for (ItemCheckout item : itens.stream().sorted(Comparator.comparing(ItemCheckout::livroId)).toList()) {
            transferirReserva.executar(
                    item.livroId(),
                    item.quantidade(),
                    OrigemReserva.CARRINHO,
                    carrinho.getId(),
                    OrigemReserva.CHECKOUT,
                    checkout.getId(),
                    checkout.getExpiraEm());
        }
        carrinhos.salvar(carrinho);
        return montarCheckout.montar(checkout);
    }

    private static ExcecaoDeConflito emAndamento() {
        return new ExcecaoDeConflito("Já existe uma compra em andamento. Volte a ela ou retorne ao carrinho.");
    }
}
