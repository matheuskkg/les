package br.fatec.les.livraria.vendas.aplicacao;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CheckoutEmAndamento;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemComPreco;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemRemovidoDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemRemovidoCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Monta o retrato do carrinho com título, autores, preço atual e subtotal (RF-VEN-001, DEC-07), além dos livros
 * retirados por expiração (RNF-VEN-001). O preço é
 * consultado no catálogo a cada leitura, pelo {@link PrecificarItens}, porque o carrinho não o congela (DEC-12,
 * DEC-16). Informa também o checkout aberto e dentro do prazo, para o cliente retomar a compra (RF-VEN-003).
 */
@Component
@RequiredArgsConstructor
public class MontarCarrinhoConsultado {

    private final PrecificarItens precificarItens;
    private final ConsultarLivrosParaVenda consultarLivros;
    private final CheckoutRepositorio checkouts;
    private final Relogio relogio;

    @Transactional(readOnly = true)
    public CarrinhoConsultado montar(Carrinho carrinho) {
        List<ItemComPreco> itens = precificarItens.executar(carrinho.getItens().stream()
                .map(item -> new ItemProposto(item.getLivroId(), item.getQuantidade()))
                .toList());
        Map<Long, LivroParaVenda> livrosRemovidos = carrinho.getRemovidos().isEmpty() ? Map.of()
                : consultarLivros.executar(carrinho.getRemovidos().stream().map(ItemRemovidoCarrinho::livroId)
                        .toList());
        List<ItemRemovidoDoCarrinho> removidos = carrinho.getRemovidos().stream()
                .map(removido -> paraRemovido(removido, livrosRemovidos))
                .toList();
        return new CarrinhoConsultado(itens, PrecificarItens.subtotal(itens), PrecificarItens.quantidadeDeItens(itens),
                carrinho.getExpiraEm(), removidos, checkoutEmAndamento(carrinho));
    }

    /**
     * Checkout aberto ainda dentro do prazo, com o mesmo filtro do {@code VerCheckout} (RF-VEN-003, RN-VEN-014,
     * DEC-12).
     */
    private @Nullable CheckoutEmAndamento checkoutEmAndamento(Carrinho carrinho) {
        return checkouts.buscarAbertoPorCliente(carrinho.getClienteId())
                .filter(checkout -> !checkout.venceuEm(relogio.agora()))
                .map(checkout -> new CheckoutEmAndamento(checkout.getExpiraEm(),
                        checkout.getItens().stream().mapToInt(ItemCheckout::quantidade).sum()))
                .orElse(null);
    }

    private static ItemRemovidoDoCarrinho paraRemovido(ItemRemovidoCarrinho removido,
            Map<Long, LivroParaVenda> livros) {
        LivroParaVenda livro = livros.get(removido.livroId());
        return new ItemRemovidoDoCarrinho(removido.livroId(), livro == null ? null : livro.titulo(),
                livro == null ? List.of() : livro.autores(), removido.quantidade(), removido.removidoEm());
    }
}
