package br.fatec.les.livraria.recomendacao.aplicacao;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.recomendacao.aplicacao.regra.LivroAVenda;
import br.fatec.les.livraria.recomendacao.dominio.calculo.ContarCoCompras;
import br.fatec.les.livraria.recomendacao.dominio.calculo.PontuarCandidatos;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AvaliacaoDada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.CompraRegistrada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroComprado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraDeLivro;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recuperação dos candidatos da vitrine, com regras determinísticas e sem IA (RNF-REC-001, DEC-03): livros ativos com
 * estoque disponível, sem os comprados e sem os avaliados negativamente, pontuados por {@link PontuarCandidatos}. As
 * referências do gosto são os livros comprados e os recomendados avaliados positivamente. As compras chegam já
 * agregadas por cliente e livro e as unidades vendidas já somadas, ambos consultados em lote no {@code vendas}
 * (RNF-GER-001); a co-compra continua sendo calculada por {@link ContarCoCompras}.
 */
@Component
@RequiredArgsConstructor
public class RecuperarCandidatos {

    private final ContarCoCompras contarCoCompras;
    private final PontuarCandidatos pontuarCandidatos;

    /**
     * @param clienteId cliente da recomendação
     * @param perfil perfil do cliente, com os comprados e as avaliações
     * @param compras compras de todos os clientes, agregadas por cliente e livro
     * @param unidadesVendidas unidades líquidas vendidas por livro
     * @param catalogo livros ativos com preço e disponível
     * @return até {@value PontuarCandidatos#LIMITE} candidatos, do mais ao menos pontuado
     */
    public List<LivroCandidato> recuperar(
            Long clienteId,
            PerfilDoCliente perfil,
            List<CompraDeLivro> compras,
            Map<Long, Integer> unidadesVendidas,
            List<LivroListado> catalogo) {
        List<CompraRegistrada> registradas =
                compras.stream()
                        .map(compra -> new CompraRegistrada(compra.clienteId(), compra.livroId(), compra.quantidade()))
                        .toList();
        Map<Long, LivroListado> porId =
                catalogo.stream().collect(Collectors.toMap(livro -> livro.livro().id(), Function.identity()));

        Set<Long> excluidos = new HashSet<>();
        perfil.livrosComprados().forEach(livro -> excluidos.add(livro.id()));
        perfil.avaliacoes().stream()
                .filter(avaliacao -> !avaliacao.positiva())
                .forEach(avaliacao -> excluidos.add(avaliacao.livroId()));

        List<LivroComprado> referencias = new ArrayList<>(perfil.livrosComprados());
        perfil.avaliacoes().stream()
                .filter(AvaliacaoDada::positiva)
                .map(avaliacao -> porId.get(avaliacao.livroId()))
                .filter(Objects::nonNull)
                .map(livro ->
                        new LivroComprado(
                                livro.livro().id(), livro.livro().titulo(), categorias(livro), livro.livro().autores()))
                .forEach(referencias::add);

        List<LivroCandidato> disponiveis =
                catalogo.stream()
                        .filter(LivroAVenda::aVenda)
                        .map(livro ->
                                new LivroCandidato(
                                        livro.livro().id(),
                                        livro.livro().titulo(),
                                        livro.livro().autores(),
                                        categorias(livro),
                                        livro.precoVenda(),
                                        BigDecimal.ZERO))
                        .toList();
        return pontuarCandidatos.pontuar(
                disponiveis, referencias, excluidos, contarCoCompras.contar(clienteId, registradas), unidadesVendidas);
    }

    private static List<String> categorias(LivroListado livro) {
        return livro.livro().categorias().stream().map(CategoriaDoLivro::nome).toList();
    }
}
