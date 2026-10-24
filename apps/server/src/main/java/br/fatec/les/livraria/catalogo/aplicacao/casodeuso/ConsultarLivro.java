package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarDisponibilidade;
import lombok.RequiredArgsConstructor;

/**
 * Consulta o detalhe de um livro ativo da vitrine (RF-LIV-005). Livro inativo ou inexistente responde 404
 * (DEC-07). O preço de venda e o disponível são calculados em lote, como na listagem.
 */
@Component
@RequiredArgsConstructor
public class ConsultarLivro {

    private final CatalogoConsultas catalogoConsultas;
    private final ConsultarPrecosDeVenda consultarPrecosDeVenda;
    private final ConsultarDisponibilidade consultarDisponibilidade;

    /**
     * @param id código do livro
     * @return o livro com preço e disponível
     * @throws ExcecaoDeRecursoNaoEncontrado se o livro não existir ou estiver inativo
     */
    @Transactional(readOnly = true)
    public LivroConsultado executar(Long id) {
        Objects.requireNonNull(id, "id");
        LivroDetalhe detalhe = catalogoConsultas.buscar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Livro não encontrado."));

        Map<Long, Dinheiro> precos = consultarPrecosDeVenda.executar(List.of(id));
        Dinheiro preco = precos.get(id);
        int disponivel = preco == null
                ? 0
                : consultarDisponibilidade.executar(List.of(id)).getOrDefault(id, 0);

        return new LivroConsultado(detalhe.id(), detalhe.titulo(), detalhe.autores(), detalhe.categorias(),
                detalhe.editora(), detalhe.ano(), preco, disponivel, detalhe.edicao(), detalhe.isbn(),
                detalhe.paginas(), detalhe.sinopse(), detalhe.altura(), detalhe.largura(),
                detalhe.profundidade(), detalhe.peso(), detalhe.codigoBarras());
    }
}
