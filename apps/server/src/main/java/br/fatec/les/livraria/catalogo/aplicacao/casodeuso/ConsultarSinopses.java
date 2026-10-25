package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.SinopseDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Ids;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao {@code recomendacao} a sinopse dos livros, para a busca do assistente por texto na sinopse
 * (RNF-REC-001, DEC-03).
 */
@Component
@RequiredArgsConstructor
public class ConsultarSinopses {

    private final CatalogoConsultas catalogoConsultas;

    /**
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return sinopse por id do livro; id inexistente não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, String> executar(Collection<Long> livroIds) {
        if (Ids.validar(livroIds)) {
            return Map.of();
        }

        Map<Long, String> sinopses = new HashMap<>();
        for (SinopseDoLivro sinopse : catalogoConsultas.buscarSinopses(livroIds)) {
            sinopses.put(sinopse.id(), sinopse.sinopse());
        }
        return Map.copyOf(sinopses);
    }
}
