package br.fatec.les.livraria.vendas.aplicacao;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarPrecosDeVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemRemovidoDoCarrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemRemovidoCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Monta o retrato do carrinho com título, autores, preço atual e subtotal (RF-VEN-001, DEC-07), além dos livros
 * retirados por expiração (RNF-VEN-001). O preço é
 * consultado em lote no catálogo a cada leitura, porque o carrinho não o congela (DEC-12, DEC-16).
 */
@Component
@RequiredArgsConstructor
public class MontarCarrinhoConsultado {

    private final ConsultarLivrosParaVenda consultarLivros;
    private final ConsultarPrecosDeVenda consultarPrecos;

    @Transactional(readOnly = true)
    public CarrinhoConsultado montar(Carrinho carrinho) {
        List<Long> livroIds = carrinho.getItens().stream().map(ItemCarrinho::getLivroId).toList();
        List<Long> todosOsLivros = Stream.concat(livroIds.stream(),
                carrinho.getRemovidos().stream().map(ItemRemovidoCarrinho::livroId)).distinct().toList();
        Map<Long, LivroParaVenda> livros = consultarLivros.executar(todosOsLivros);
        Map<Long, Dinheiro> precos = consultarPrecos.executar(livroIds);

        List<ItemDoCarrinho> itens = carrinho.getItens().stream()
                .map(item -> paraItem(item, livros, precos))
                .toList();

        Dinheiro subtotal = itens.stream()
                .map(ItemDoCarrinho::subtotal)
                .filter(Objects::nonNull)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
        int quantidadeDeItens = itens.stream().mapToInt(ItemDoCarrinho::quantidade).sum();

        List<ItemRemovidoDoCarrinho> removidos = carrinho.getRemovidos().stream()
                .map(removido -> paraRemovido(removido, livros))
                .toList();
        return new CarrinhoConsultado(itens, subtotal, quantidadeDeItens, carrinho.getExpiraEm(), removidos);
    }

    private static ItemRemovidoDoCarrinho paraRemovido(ItemRemovidoCarrinho removido,
            Map<Long, LivroParaVenda> livros) {
        LivroParaVenda livro = livros.get(removido.livroId());
        return new ItemRemovidoDoCarrinho(removido.livroId(), livro == null ? null : livro.titulo(),
                livro == null ? List.of() : livro.autores(), removido.quantidade(), removido.removidoEm());
    }

    private static ItemDoCarrinho paraItem(ItemCarrinho item, Map<Long, LivroParaVenda> livros,
            Map<Long, Dinheiro> precos) {
        LivroParaVenda livro = livros.get(item.getLivroId());
        String titulo = livro == null ? null : livro.titulo();
        List<String> autores = livro == null ? List.of() : livro.autores();
        // Sem preço (livro fora de venda), o item aparece sem valor e fica fora do subtotal.
        Dinheiro preco = precos.get(item.getLivroId());
        return new ItemDoCarrinho(item.getLivroId(), titulo, autores, preco, item.getQuantidade(),
                preco == null ? null : preco.multiplicar(item.getQuantidade()));
    }
}
