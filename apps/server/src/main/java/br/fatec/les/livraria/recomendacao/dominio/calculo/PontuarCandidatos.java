package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;

/**
 * Escolhe os candidatos da vitrine (RNF-REC-001, DEC-03). Ficam de fora os livros excluídos (comprados ou avaliados
 * negativamente). Com referências, a pontuação soma, por candidato, as categorias e os autores em comum com cada
 * livro de referência (comprados e recomendados avaliados positivamente), sem contar o próprio candidato, e o número de clientes da co-compra. Sem
 * referências, a pontuação é o total vendido. Empates vão para o mais vendido e depois para o menor id. Os
 * {@value #LIMITE} de maior pontuação vão à IA.
 */
@Component
public class PontuarCandidatos {

    public static final int LIMITE = 8;

    /**
     * @param disponiveis livros ativos com estoque disponível e preço, com pontuação ainda não calculada
     * @param referencias livros que representam o gosto do cliente
     * @param excluidos   ids que não podem ser recomendados
     * @param coCompras   clientes da co-compra por livro
     * @param vendidos    unidades vendidas por livro
     * @return até {@value #LIMITE} candidatos com a pontuação, do maior para o menor
     */
    public List<LivroCandidato> pontuar(List<LivroCandidato> disponiveis, List<LivroComprado> referencias,
            Set<Long> excluidos, Map<Long, Integer> coCompras, Map<Long, Integer> vendidos) {
        boolean semReferencias = referencias.isEmpty();
        return disponiveis.stream()
                .filter(livro -> !excluidos.contains(livro.id()))
                .map(livro -> comPontuacao(livro, semReferencias
                        ? vendidos.getOrDefault(livro.id(), 0)
                        : emComum(livro, referencias) + coCompras.getOrDefault(livro.id(), 0)))
                .sorted(Comparator.comparing(LivroCandidato::pontuacao).reversed()
                        .thenComparing(livro -> vendidos.getOrDefault(livro.id(), 0), Comparator.reverseOrder())
                        .thenComparing(LivroCandidato::id))
                .limit(LIMITE)
                .toList();
    }

    /** Categorias e autores em comum com as referências, sem contar o próprio livro (avaliado positivamente). */
    private static int emComum(LivroCandidato livro, List<LivroComprado> referencias) {
        List<LivroComprado> outras = referencias.stream().filter(referencia -> !referencia.id().equals(livro.id()))
                .toList();
        return soma(livro.categorias(), frequencias(outras.stream().map(LivroComprado::categorias).toList()))
                + soma(livro.autores(), frequencias(outras.stream().map(LivroComprado::autores).toList()));
    }

    private static Map<String, Integer> frequencias(List<List<String>> listas) {
        Map<String, Integer> frequencias = new HashMap<>();
        listas.forEach(lista -> Set.copyOf(lista).forEach(valor -> frequencias.merge(valor, 1, Integer::sum)));
        return frequencias;
    }

    private static int soma(Collection<String> valores, Map<String, Integer> frequencias) {
        return Set.copyOf(valores).stream().mapToInt(valor -> frequencias.getOrDefault(valor, 0)).sum();
    }

    private static LivroCandidato comPontuacao(LivroCandidato livro, int pontuacao) {
        return new LivroCandidato(livro.id(), livro.titulo(), livro.autores(), livro.categorias(), livro.preco(),
                BigDecimal.valueOf(pontuacao));
    }
}
