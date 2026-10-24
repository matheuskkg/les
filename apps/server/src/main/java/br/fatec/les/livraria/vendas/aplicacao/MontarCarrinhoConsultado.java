package br.fatec.les.livraria.vendas.aplicacao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarPrecosDeVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemDoCarrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Monta o retrato do carrinho com título, autores, preço atual e subtotal (RF-VEN-001, DEC-07). O preço é
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
        Map<Long, LivroParaVenda> livros = consultarLivros.executar(livroIds);
        Map<Long, Dinheiro> precos = consultarPrecos.executar(livroIds);

        List<ItemDoCarrinho> itens = carrinho.getItens().stream()
                .map(item -> paraItem(item, livros, precos))
                .toList();

        Dinheiro subtotal = itens.stream()
                .map(ItemDoCarrinho::subtotal)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
        int quantidadeDeItens = itens.stream().mapToInt(ItemDoCarrinho::quantidade).sum();

        return new CarrinhoConsultado(itens, subtotal, quantidadeDeItens, carrinho.getExpiraEm());
    }

    private static ItemDoCarrinho paraItem(ItemCarrinho item, Map<Long, LivroParaVenda> livros,
            Map<Long, Dinheiro> precos) {
        LivroParaVenda livro = livros.get(item.getLivroId());
        String titulo = livro == null ? null : livro.titulo();
        List<String> autores = livro == null ? List.of() : livro.autores();
        Dinheiro preco = precos.getOrDefault(item.getLivroId(), Dinheiro.ZERO);
        return new ItemDoCarrinho(item.getLivroId(), titulo, autores, preco, item.getQuantidade(),
                preco.multiplicar(item.getQuantidade()));
    }
}
