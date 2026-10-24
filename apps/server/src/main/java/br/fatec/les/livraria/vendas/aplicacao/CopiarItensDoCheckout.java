package br.fatec.les.livraria.vendas.aplicacao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;
import br.fatec.les.livraria.vendas.dominio.pedido.CategoriaNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.ItemPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.LivroNoPedido;
import lombok.RequiredArgsConstructor;

/**
 * Copia os itens do checkout para o pedido com os dados do livro e o preço de venda do momento da finalização
 * (RF-VEN-008, RN-EST-002, DEC-12, DEC-16). Livro que saiu de venda impede a finalização.
 */
@Component
@RequiredArgsConstructor
public class CopiarItensDoCheckout {

    private final ConsultarLivro consultarLivro;

    /** @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se algum livro não tiver preço */
    @Transactional(readOnly = true)
    public List<ItemPedido> executar(List<ItemCheckout> itens) {
        Violacoes violacoes = new Violacoes();
        List<ItemPedido> copiados = new ArrayList<>();
        for (ItemCheckout item : itens) {
            LivroConsultado consultado = consultarLivro.executar(item.livroId());
            LivroNoPedido livro = copia(consultado.livro());
            if (consultado.precoVenda() == null) {
                violacoes.adicionar("RF-VEN-008", "\"%s\" não está mais à venda. Volte ao carrinho para removê-lo."
                        .formatted(livro.titulo()));
            } else {
                copiados.add(new ItemPedido(null, livro, consultado.precoVenda(), item.quantidade()));
            }
        }
        violacoes.lancarSeHouver();
        return copiados;
    }

    private static LivroNoPedido copia(LivroDetalhe detalhe) {
        LivroResumo resumo = detalhe.livro();
        return new LivroNoPedido(resumo.id(), resumo.titulo(), resumo.autores(), resumo.editora(), detalhe.edicao(),
                detalhe.isbn(), resumo.categorias().stream()
                        .map(categoria -> new CategoriaNoPedido(categoria.id(), categoria.nome()))
                        .toList());
    }
}
