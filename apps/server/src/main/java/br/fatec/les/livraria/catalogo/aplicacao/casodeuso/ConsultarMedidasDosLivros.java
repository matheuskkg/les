package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.MedidasDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Ids;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao {@code vendas} o peso e as dimensões dos livros, inclusive os inativos, para o cálculo de frete
 * (RF-VEN-004, DEC-05.3).
 */
@Component
@RequiredArgsConstructor
public class ConsultarMedidasDosLivros {

    private final CatalogoConsultas catalogoConsultas;

    /**
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return medidas por id do livro; id inexistente não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, MedidasDoLivro> executar(Collection<Long> livroIds) {
        if (Ids.validar(livroIds)) {
            return Map.of();
        }

        Map<Long, MedidasDoLivro> medidas = new HashMap<>();
        for (MedidasDoLivro medida : catalogoConsultas.buscarMedidas(livroIds)) {
            medidas.put(medida.id(), medida);
        }
        return Map.copyOf(medidas);
    }
}
