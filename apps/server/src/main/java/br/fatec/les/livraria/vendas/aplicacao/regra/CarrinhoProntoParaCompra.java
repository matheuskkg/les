package br.fatec.les.livraria.vendas.aplicacao.regra;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarPrecosDeVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Só um carrinho com itens, todos ativos e com preço de venda, vai para o pagamento (RF-VEN-003, RN-LIV-001,
 * RN-EST-002, DEC-12). O estoque é conferido depois, ao passar as reservas ao checkout (RN-VEN-001).
 */
@Component
@RequiredArgsConstructor
public class CarrinhoProntoParaCompra implements Regra<Carrinho> {

    private final ConsultarLivrosParaVenda consultarLivros;
    private final ConsultarPrecosDeVenda consultarPrecos;

    @Override
    public void validar(Carrinho alvo, Violacoes violacoes) {
        if (alvo.vazio()) {
            violacoes.adicionar("RF-VEN-003", "O carrinho está vazio.");
            return;
        }
        List<Long> livroIds = alvo.getItens().stream().map(ItemCarrinho::getLivroId).toList();
        Map<Long, LivroParaVenda> livros = consultarLivros.executar(livroIds);
        Map<Long, Dinheiro> precos = consultarPrecos.executar(livroIds);
        for (Long livroId : livroIds) {
            LivroParaVenda livro = livros.get(livroId);
            if (livro == null || !livro.ativo() || !precos.containsKey(livroId)) {
                String nome = livro == null ? "Livro " + livroId : livro.titulo();
                violacoes.adicionar(
                        "RF-VEN-003", "\"%s\" não está disponível para venda. Remova-o do carrinho.".formatted(nome));
            }
        }
    }
}
