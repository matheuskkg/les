package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.math.BigDecimal;

/** Projeção da margem do grupo de precificação por livro (RN-LIV-003, RN-EST-002). */
public interface MargemDoLivroProjecao {

    Long getLivroId();

    BigDecimal getMargem();
}
