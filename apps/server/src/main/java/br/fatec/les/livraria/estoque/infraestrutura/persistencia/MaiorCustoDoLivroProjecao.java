package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.math.BigDecimal;

/** Projeção da agregação de maior custo por livro (RN-EST-002). */
public interface MaiorCustoDoLivroProjecao {

    Long getLivroId();

    BigDecimal getMaiorCusto();
}
