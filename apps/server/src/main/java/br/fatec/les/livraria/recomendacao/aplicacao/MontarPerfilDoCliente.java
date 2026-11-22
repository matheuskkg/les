package br.fatec.les.livraria.recomendacao.aplicacao;

import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Perfil enviado à IA (RNF-REC-001, DEC-03): os livros comprados em pedidos que contam como venda, as {@value
 * #FREQUENTES} categorias e autores mais frequentes entre eles e as avaliações dadas aos itens recomendados. As compras
 * do cliente já chegam filtradas pelo caso de uso que consulta {@code vendas} (RNF-GER-001); aqui fica a ordenação pela
 * compra mais recente e o desconto das repetições.
 */
@Component
public class MontarPerfilDoCliente {

    static final int FREQUENTES = 5;

    /**
     * @param compras compras do cliente em pedidos que contam como venda
     * @param avaliacoes última avaliação de cada livro recomendado ao cliente
     * @return o perfil do cliente
     */
    public PerfilDoCliente montar(List<LivroVendido> compras, List<AvaliacaoDada> avaliacoes) {
        Map<Long, LivroComprado> comprados = new LinkedHashMap<>();
        compras.stream()
                .sorted(Comparator.comparing(LivroVendido::realizadoEm).reversed())
                .forEach(compra ->
                        comprados.putIfAbsent(
                                compra.livroId(),
                                new LivroComprado(
                                        compra.livroId(), compra.titulo(), compra.categorias(), compra.autores())));
        List<LivroComprado> livros = List.copyOf(comprados.values());
        return new PerfilDoCliente(
                livros,
                maisFrequentes(livros.stream().flatMap(livro -> livro.categorias().stream()).toList()),
                maisFrequentes(livros.stream().flatMap(livro -> livro.autores().stream()).toList()),
                avaliacoes);
    }

    private static List<String> maisFrequentes(List<String> valores) {
        Map<String, Long> contagem = new HashMap<>();
        valores.forEach(valor -> contagem.merge(valor, 1L, Long::sum));
        return contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(FREQUENTES)
                .map(Map.Entry::getKey)
                .toList();
    }
}
