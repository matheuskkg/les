package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.math.BigDecimal;

/** Projeção do peso e das dimensões de um livro pelo id, sem carregar a entidade (RF-VEN-004, RNF-GER-001). */
public interface MedidasDoLivroProjecao {

    Long getLivroId();

    BigDecimal getPeso();

    BigDecimal getAltura();

    BigDecimal getLargura();

    BigDecimal getProfundidade();
}
