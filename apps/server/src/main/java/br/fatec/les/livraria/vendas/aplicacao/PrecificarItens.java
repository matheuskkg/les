package br.fatec.les.livraria.vendas.aplicacao;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarPrecosDeVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemComPreco;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import lombok.RequiredArgsConstructor;

/**
 * Completa os itens do carrinho ou do checkout com título, autores e o preço de venda atual, consultados em lote no
 * catálogo (RF-VEN-001, RF-VEN-003, DEC-12, DEC-16). Livro sem preço (fora de venda) aparece sem valor e fica fora do
 * subtotal.
 */
@Component
@RequiredArgsConstructor
public class PrecificarItens {

    private final ConsultarLivrosParaVenda consultarLivros;
    private final ConsultarPrecosDeVenda consultarPrecos;

    @Transactional(readOnly = true)
    public List<ItemComPreco> executar(List<ItemProposto> itens) {
        List<Long> livroIds = itens.stream().map(ItemProposto::livroId).toList();
        Map<Long, LivroParaVenda> livros = consultarLivros.executar(livroIds);
        Map<Long, Dinheiro> precos = consultarPrecos.executar(livroIds);
        return itens.stream().map(item -> paraItem(item, livros, precos)).toList();
    }

    /** Soma dos subtotais dos itens com preço; zero sem itens. */
    public static Dinheiro subtotal(List<ItemComPreco> itens) {
        return itens.stream()
                .map(ItemComPreco::subtotal)
                .filter(Objects::nonNull)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    /** Soma das unidades dos itens. */
    public static int quantidadeDeItens(List<ItemComPreco> itens) {
        return itens.stream().mapToInt(ItemComPreco::quantidade).sum();
    }

    private static ItemComPreco paraItem(ItemProposto item, Map<Long, LivroParaVenda> livros,
            Map<Long, Dinheiro> precos) {
        LivroParaVenda livro = livros.get(item.livroId());
        Dinheiro preco = precos.get(item.livroId());
        return new ItemComPreco(item.livroId(), livro == null ? null : livro.titulo(),
                livro == null ? List.of() : livro.autores(), preco, item.quantidade(),
                preco == null ? null : preco.multiplicar(item.quantidade()));
    }
}
