package br.fatec.les.livraria.vendas.aplicacao.regra;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarPrecosDeVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import lombok.RequiredArgsConstructor;

/**
 * Só um livro ativo e com preço de venda pode entrar no carrinho (RF-VEN-001, RN-LIV-001, RN-EST-002). O
 * livro é consultado no catálogo e o preço é calculado pelo caso de uso público do catálogo. O estoque é
 * conferido depois, ao reservar (RN-VEN-001).
 */
@Component
@RequiredArgsConstructor
public class LivroDoCarrinho implements Regra<ItemProposto> {

    private final ConsultarLivrosParaVenda consultarLivros;
    private final ConsultarPrecosDeVenda consultarPrecos;

    @Override
    public void validar(ItemProposto alvo, Violacoes violacoes) {
        if (alvo.livroId() == null) {
            violacoes.adicionar("RF-VEN-001", "Informe o livro.");
            return;
        }

        Map<Long, LivroParaVenda> livros = consultarLivros.executar(List.of(alvo.livroId()));
        LivroParaVenda livro = livros.get(alvo.livroId());
        if (livro == null) {
            violacoes.adicionar("RF-VEN-001", "Livro não encontrado.");
            return;
        }
        if (!livro.ativo() || !consultarPrecos.executar(List.of(alvo.livroId())).containsKey(alvo.livroId())) {
            violacoes.adicionar("RF-VEN-001", "O livro não está disponível para venda.");
        }
    }
}
