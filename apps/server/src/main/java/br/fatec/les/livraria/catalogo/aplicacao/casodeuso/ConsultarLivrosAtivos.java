package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import br.fatec.les.livraria.catalogo.aplicacao.EnriquecerComPrecoEDisponibilidade;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Ids;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Expõe ao {@code recomendacao} os livros ativos pelos ids, com preço e disponível (RNF-REC-001, DEC-03): exibir os
 * citados numa conversa ou os recomendados na vitrine não lê o catálogo inteiro, só o lote pedido (RNF-GER-001).
 */
@Component
@RequiredArgsConstructor
public class ConsultarLivrosAtivos {

    private final CatalogoConsultas catalogoConsultas;
    private final EnriquecerComPrecoEDisponibilidade enriquecerComPrecoEDisponibilidade;

    /**
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return livro ativo por id, com preço de venda e disponível; inativo ou inexistente não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, LivroListado> executar(Collection<Long> livroIds) {
        if (Ids.validar(livroIds)) {
            return Map.of();
        }

        List<LivroListado> livros =
                enriquecerComPrecoEDisponibilidade.enriquecer(catalogoConsultas.buscarAtivos(livroIds));
        Map<Long, LivroListado> porId = new HashMap<>();
        for (LivroListado livro : livros) {
            porId.put(livro.livro().id(), livro);
        }
        return Map.copyOf(porId);
    }
}
