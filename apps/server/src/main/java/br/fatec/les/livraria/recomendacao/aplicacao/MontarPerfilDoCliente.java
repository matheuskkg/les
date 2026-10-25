package br.fatec.les.livraria.recomendacao.aplicacao;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;

/**
 * Perfil enviado à IA (RNF-REC-001, DEC-03): os livros comprados em pedidos que contam como venda, as
 * {@value #FREQUENTES} categorias e autores mais frequentes entre eles e as avaliações dadas aos itens recomendados.
 */
@Component
public class MontarPerfilDoCliente {

    static final int FREQUENTES = 5;

    /**
     * @param clienteId  cliente do perfil
     * @param vendidos   livros vendidos de todos os clientes
     * @param avaliacoes última avaliação de cada livro recomendado ao cliente
     */
    public PerfilDoCliente montar(Long clienteId, List<LivroVendido> vendidos, List<AvaliacaoDada> avaliacoes) {
        Map<Long, LivroComprado> comprados = new LinkedHashMap<>();
        vendidos.stream()
                .filter(vendido -> vendido.clienteId().equals(clienteId))
                .sorted(Comparator.comparing(LivroVendido::realizadoEm).reversed())
                .forEach(vendido -> comprados.putIfAbsent(vendido.livroId(), new LivroComprado(vendido.livroId(),
                        vendido.titulo(), vendido.categorias(), vendido.autores())));
        List<LivroComprado> livros = List.copyOf(comprados.values());
        return new PerfilDoCliente(livros,
                maisFrequentes(livros.stream().flatMap(livro -> livro.categorias().stream()).toList()),
                maisFrequentes(livros.stream().flatMap(livro -> livro.autores().stream()).toList()),
                avaliacoes);
    }

    private static List<String> maisFrequentes(List<String> valores) {
        Map<String, Long> contagem = new HashMap<>();
        valores.forEach(valor -> contagem.merge(valor, 1L, Long::sum));
        return contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(FREQUENTES)
                .map(Map.Entry::getKey)
                .toList();
    }
}
