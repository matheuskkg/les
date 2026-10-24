package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.LiberarItensDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.PropriedadesDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.ReservarItensDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;
import lombok.RequiredArgsConstructor;

/**
 * "Voltar ao carrinho" (DEC-12): encerra o checkout aberto e devolve os itens ao carrinho, somados aos que já
 * estiverem lá. O bloqueio do carrinho recomeça (DEC-05.1) e todos os itens são reservados de novo, com a conferência
 * do estoque (RN-VEN-001).
 *
 * <p>Trava o carrinho antes do checkout, na mesma ordem do {@link IniciarCheckout} e da expiração.</p>
 */
@Component
@RequiredArgsConstructor
public class VoltarAoCarrinho {

    private final IdentificarCliente identificarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final CheckoutRepositorio checkouts;
    private final LiberarItensDoCheckout liberarItensDoCheckout;
    private final ReservarItensDoCarrinho reservarItens;
    private final PropriedadesDoCarrinho propriedades;
    private final Relogio relogio;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @return o carrinho com os itens devolvidos
     * @throws ExcecaoDeRecursoNaoEncontrado se não houver checkout aberto
     * @throws ExcecaoDeNegocio              com {@code RN-VEN-001} se faltar estoque para a soma dos itens
     */
    @Transactional
    public CarrinhoConsultado executar() {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho = carrinhos.buscarPorClienteParaAlterar(clienteId)
                .orElseGet(() -> carrinhos.salvar(Carrinho.vazio(clienteId)));
        Checkout checkout = checkouts.buscarAbertoPorClienteParaAlterar(clienteId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Nenhuma compra em andamento."));

        liberarItensDoCheckout.executar(checkout);
        try {
            for (ItemCheckout item : checkout.getItens()) {
                carrinho.adicionar(item.livroId(), item.quantidade());
            }
        } catch (ArithmeticException excecao) {
            throw new ExcecaoDeNegocio("RN-VEN-001", "Quantidade acima do estoque disponível.");
        }
        LocalDateTime expiraEm = relogio.agora().plus(propriedades.prazoBloqueio());
        carrinho.renovarExpiracao(expiraEm);
        try {
            reservarItens.executar(carrinho, expiraEm);
        } catch (ExcecaoDeNegocio excecao) {
            throw new ExcecaoDeNegocio("RN-VEN-001", "Não há estoque para somar os itens da compra aos do carrinho. "
                    + "Diminua ou remova no carrinho os livros repetidos e tente de novo.");
        }
        checkout.encerrar();
        checkouts.salvar(checkout);
        return montarCarrinho.montar(carrinhos.salvar(carrinho));
    }
}
