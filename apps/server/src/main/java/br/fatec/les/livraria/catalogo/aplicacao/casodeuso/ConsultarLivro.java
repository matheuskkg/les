package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import br.fatec.les.livraria.catalogo.aplicacao.EnriquecerComPrecoEDisponibilidade;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Consulta o detalhe de um livro ativo da vitrine (RF-LIV-005). Livro inativo ou inexistente responde 404 (DEC-07). O
 * preço e o disponível são enriquecidos por {@link EnriquecerComPrecoEDisponibilidade}, a mesma regra da listagem.
 */
@Component
@RequiredArgsConstructor
public class ConsultarLivro {

    private final CatalogoConsultas catalogoConsultas;
    private final EnriquecerComPrecoEDisponibilidade enriquecerComPrecoEDisponibilidade;

    /**
     * @param id código do livro
     * @return o livro com preço e disponível
     * @throws ExcecaoDeRecursoNaoEncontrado se o livro não existir ou estiver inativo
     */
    @Transactional(readOnly = true)
    public LivroConsultado executar(Long id) {
        Objects.requireNonNull(id, "id");
        LivroDetalhe detalhe =
                catalogoConsultas
                        .buscar(id)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Livro não encontrado."));
        return enriquecerComPrecoEDisponibilidade.enriquecer(detalhe);
    }
}
