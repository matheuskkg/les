package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.List;

/**
 * Livro que o cliente já comprou, usado como contexto da recomendação (RNF-REC-001, DEC-03). As categorias e os
 * autores acompanham o título para que o perfil seja montado sem nova consulta ao catálogo.
 */
public record LivroComprado(
        Long id,
        String titulo,
        List<String> categorias,
        List<String> autores) {

    public LivroComprado {
        categorias = List.copyOf(categorias);
        autores = List.copyOf(autores);
    }
}
