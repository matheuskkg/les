package br.fatec.les.livraria.catalogo.aplicacao;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarPrecosDeVenda;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarDisponibilidade;

import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enriquece livros da vitrine com o preço de venda (RF-EST-002, RN-EST-002) e o disponível (DEC-15.3), consultando os
 * dois em lote, sem uma consulta por item. É o único lugar da regra: livro sem preço aparece sem {@code precoVenda} e
 * com disponível zero, e a disponibilidade só é consultada para os livros que têm preço.
 */
@Component
@RequiredArgsConstructor
public class EnriquecerComPrecoEDisponibilidade {

    private final ConsultarPrecosDeVenda consultarPrecosDeVenda;
    private final ConsultarDisponibilidade consultarDisponibilidade;

    /** Enriquece uma página da listagem, preservando o envelope da paginação. */
    @Transactional(readOnly = true)
    public Pagina<LivroListado> enriquecer(Pagina<LivroResumo> pagina) {
        Map<Long, PrecoEDisponibilidade> valores = calcular(pagina.itens().stream().map(LivroResumo::id).toList());
        return pagina.map(livro -> {
            PrecoEDisponibilidade valor = valores.get(livro.id());
            return new LivroListado(livro, valor.precoVenda(), valor.disponivel());
        });
    }

    /** Enriquece o detalhe de um livro. */
    @Transactional(readOnly = true)
    public LivroConsultado enriquecer(LivroDetalhe livro) {
        Long livroId = livro.livro().id();
        PrecoEDisponibilidade valor = calcular(List.of(livroId)).get(livroId);
        return new LivroConsultado(livro, valor.precoVenda(), valor.disponivel());
    }

    /** Enriquece os livros informados, sem o envelope da paginação. */
    @Transactional(readOnly = true)
    public List<LivroListado> enriquecer(List<LivroResumo> livros) {
        if (livros.isEmpty()) {
            return List.of();
        }
        Map<Long, PrecoEDisponibilidade> valores = calcular(livros.stream().map(LivroResumo::id).toList());
        return livros.stream()
                .map(livro -> {
                    PrecoEDisponibilidade valor = valores.get(livro.id());
                    return new LivroListado(livro, valor.precoVenda(), valor.disponivel());
                })
                .toList();
    }

    private Map<Long, PrecoEDisponibilidade> calcular(List<Long> livroIds) {
        if (livroIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Dinheiro> precos = consultarPrecosDeVenda.executar(livroIds);
        Map<Long, Integer> disponiveis = consultarDisponibilidade.executar(precos.keySet());

        Map<Long, PrecoEDisponibilidade> valores = new HashMap<>();
        for (Long livroId : livroIds) {
            Dinheiro preco = precos.get(livroId);
            int disponivel = preco == null ? 0 : disponiveis.getOrDefault(livroId, 0);
            valores.put(livroId, new PrecoEDisponibilidade(preco, disponivel));
        }
        return Map.copyOf(valores);
    }

    /** Preço e disponível de um livro; {@code precoVenda} é nulo quando o livro não tem preço. */
    private record PrecoEDisponibilidade(@Nullable Dinheiro precoVenda, int disponivel) {}
}
