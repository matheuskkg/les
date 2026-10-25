package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.List;

/**
 * Perfil enviado à IA a cada chamada (RNF-REC-001, DEC-03): os livros comprados, as categorias e os autores mais
 * frequentes e as avaliações dadas. É o "treino pelo contexto" do DEC-03, montado pelo server com regras
 * determinísticas.
 */
public record PerfilDoCliente(
        List<LivroComprado> livrosComprados,
        List<String> categoriasFrequentes,
        List<String> autoresFrequentes,
        List<AvaliacaoDada> avaliacoes) {

    public PerfilDoCliente {
        livrosComprados = List.copyOf(livrosComprados);
        categoriasFrequentes = List.copyOf(categoriasFrequentes);
        autoresFrequentes = List.copyOf(autoresFrequentes);
        avaliacoes = List.copyOf(avaliacoes);
    }

    /** Perfil sem histórico de compras, como o do visitante ou do cliente que ainda não comprou. */
    public boolean semLivrosComprados() {
        return livrosComprados.isEmpty();
    }
}
