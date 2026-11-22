package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Busca categorias pelos ids, para outros módulos conferirem e nomearem as escolhidas (RF-ANA-003, RN-LIV-002). */
@Component
@RequiredArgsConstructor
public class ConsultarCategorias {

    private final CatalogoConsultas catalogoConsultas;

    /**
     * @param ids ids desejados; não pode ser nulo
     * @return as categorias existentes entre as informadas, em qualquer ordem
     */
    @Transactional(readOnly = true)
    public List<CategoriaDoLivro> executar(Collection<Long> ids) {
        Objects.requireNonNull(ids, "ids");
        return catalogoConsultas.buscarCategorias(ids);
    }
}
