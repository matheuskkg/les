package br.fatec.les.livraria.recomendacao.aplicacao;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarSinopses;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.DetalheDoLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.LivroDoHistorico;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.aplicacao.regra.LivroAVenda;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarLivrosVendidos;
import lombok.RequiredArgsConstructor;

/**
 * Monta as tools só de leitura do chat para um cliente (RNF-REC-001, DEC-03), sobre os casos de uso públicos do
 * {@code catalogo} e do {@code vendas}. O id do cliente é resolvido pelo caso de uso a partir do
 * {@code UsuarioAtual} e fica preso nas tools; o modelo nunca o informa.
 *
 * <p>O catálogo é pequeno (seed), então as tools leem os livros ativos uma vez por mensagem e filtram em memória,
 * sem diferenciar maiúsculas nem acentos. A busca exige todas as palavras do texto, cada uma no título, num autor,
 * numa categoria ou na sinopse. Só livros ativos, com preço e com estoque aparecem.</p>
 */
@Component
@RequiredArgsConstructor
public class FerramentasDoCatalogo {

    static final int LIMITE_DA_BUSCA = 10;
    /** Termos menores, como "de" e "o", não filtram. */
    static final int TAMANHO_MINIMO_DO_TERMO = 3;

    private final LerCatalogo lerCatalogo;
    private final ConsultarSinopses consultarSinopses;
    private final ConsultarLivrosVendidos consultarLivrosVendidos;

    /** Tools do cliente informado, que vem da sessão. */
    public FerramentasDoAssistente doCliente(Long clienteId) {
        return new Ferramentas(clienteId);
    }

    private final class Ferramentas implements FerramentasDoAssistente {

        private final Long clienteId;
        private @Nullable Map<Long, LivroListado> aVenda;

        private Ferramentas(Long clienteId) {
            this.clienteId = clienteId;
        }

        /** Os livros à venda, lidos uma vez por mensagem, mesmo que o modelo chame várias tools. */
        private Map<Long, LivroListado> aVenda() {
            if (aVenda == null) {
                aVenda = lerCatalogo.ativos().stream()
                        .filter(LivroAVenda::aVenda)
                        .collect(Collectors.toMap(livro -> livro.livro().id(), Function.identity(),
                                (primeiro, segundo) -> primeiro, LinkedHashMap::new));
            }
            return aVenda;
        }

        @Override
        public List<ResumoDeLivro> buscarLivros(String texto, @Nullable String categoria,
                @Nullable BigDecimal precoMinimo, @Nullable BigDecimal precoMaximo) {
            Map<Long, String> sinopses = consultarSinopses.executar(aVenda().keySet());
            List<String> termos = termos(texto);
            String categoriaProcurada = TextoNormalizado.de(categoria);
            return aVenda().values().stream()
                    .filter(livro -> termos.stream().allMatch(termo ->
                            contem(livro, sinopses.get(livro.livro().id()), termo)))
                    .filter(livro -> categoriaProcurada.isEmpty() || livro.livro().categorias().stream()
                            .anyMatch(atual -> TextoNormalizado.de(atual.nome()).contains(categoriaProcurada)))
                    .filter(livro -> precoMinimo == null || livro.precoVenda().valor().compareTo(precoMinimo) >= 0)
                    .filter(livro -> precoMaximo == null || livro.precoVenda().valor().compareTo(precoMaximo) <= 0)
                    .limit(LIMITE_DA_BUSCA)
                    .map(FerramentasDoCatalogo::resumo)
                    .toList();
        }

        @Override
        public Optional<DetalheDoLivro> verLivro(Long livroId) {
            if (livroId == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(aVenda().get(livroId))
                    .map(livro -> new DetalheDoLivro(resumo(livro),
                            consultarSinopses.executar(List.of(livroId)).getOrDefault(livroId, ""),
                            livro.disponivel()));
        }

        @Override
        public List<LivroDoHistorico> verMeuHistorico() {
            return consultarLivrosVendidos.executar().stream()
                    .filter(vendido -> vendido.clienteId().equals(clienteId))
                    .map(vendido -> new LivroDoHistorico(vendido.livroId(), vendido.titulo()))
                    .distinct()
                    .toList();
        }
    }

    /**
     * Palavras do texto procurado, normalizadas; o livro precisa conter todas, em qualquer campo, para o modelo
     * poder buscar "machado assis" ou "fantasia anões".
     */
    private static List<String> termos(@Nullable String texto) {
        return Arrays.stream(TextoNormalizado.de(texto).split("[^\\p{L}\\p{N}]+"))
                .filter(termo -> termo.length() >= TAMANHO_MINIMO_DO_TERMO)
                .toList();
    }

    private static boolean contem(LivroListado livro, @Nullable String sinopse, String termo) {
        return TextoNormalizado.de(livro.livro().titulo()).contains(termo)
                || livro.livro().autores().stream().anyMatch(autor -> TextoNormalizado.de(autor).contains(termo))
                || livro.livro().categorias().stream()
                        .anyMatch(atual -> TextoNormalizado.de(atual.nome()).contains(termo))
                || TextoNormalizado.de(sinopse).contains(termo);
    }

    private static ResumoDeLivro resumo(LivroListado livro) {
        return new ResumoDeLivro(livro.livro().id(), livro.livro().titulo(), livro.livro().autores(),
                livro.livro().categorias().stream().map(CategoriaDoLivro::nome).toList(), livro.precoVenda());
    }
}
