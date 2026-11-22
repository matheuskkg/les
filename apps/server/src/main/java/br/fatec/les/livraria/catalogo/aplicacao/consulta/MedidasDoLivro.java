package br.fatec.les.livraria.catalogo.aplicacao.consulta;

import java.math.BigDecimal;

/**
 * Peso e dimensões do livro, usados pelo {@code vendas} no cálculo de frete (RF-VEN-004, RN-LIV-001, DEC-05.3).
 *
 * @param id id do livro
 * @param peso peso real, em kg
 * @param altura altura, em cm
 * @param largura largura, em cm
 * @param profundidade profundidade, em cm
 */
public record MedidasDoLivro(
        Long id, BigDecimal peso, BigDecimal altura, BigDecimal largura, BigDecimal profundidade) {}
