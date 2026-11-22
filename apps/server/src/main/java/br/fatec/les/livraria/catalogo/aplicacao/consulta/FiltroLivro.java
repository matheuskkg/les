package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.util.List;
import java.util.Objects;

/**
 * Filtro combinado da consulta de livros da vitrine (RF-LIV-005, DEC-07). Os campos informados se combinam com E;
 * categorias repetidas se combinam com OU. Texto em branco é ignorado pelo adapter, e intervalo com início maior que o
 * fim causa HTTP 400.
 *
 * @param titulo trecho do título, sem diferenciar maiúsculas nem acentos
 * @param autor trecho do nome de um dos autores
 * @param categoria ids de categoria; vazio não filtra
 * @param editora trecho do nome da editora
 * @param isbn ISBN comparado só pelos dígitos
 * @param codigoBarras código de barras exato
 * @param anoDe ano inicial, inclusive
 * @param anoAte ano final, inclusive
 * @param edicao trecho da edição
 * @param paginasDe número mínimo de páginas
 * @param paginasAte número máximo de páginas
 * @param codigo código do livro, que é o próprio id (RNF-LIV-001, DEC-14)
 * @param q busca livre: trecho do título, de um autor, da editora ou do ISBN, combinados com OU
 */
public record FiltroLivro(
        String titulo,
        String autor,
        List<Long> categoria,
        String editora,
        String isbn,
        String codigoBarras,
        Integer anoDe,
        Integer anoAte,
        String edicao,
        Integer paginasDe,
        Integer paginasAte,
        Long codigo,
        String q) {

    public FiltroLivro {
        categoria = categoria == null ? List.of() : categoria.stream().filter(Objects::nonNull).toList();
    }
}
