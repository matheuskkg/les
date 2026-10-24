package br.fatec.les.livraria.estoque.dominio.calculo;

import org.springframework.stereotype.Component;

/**
 * Calcula o saldo disponível de um livro: quantidade em mãos menos as reservas ativas, nunca negativo
 * (DEC-15.3). As reservas vencidas não entram na conta, porque são liberadas na expiração (RN-EST-006).
 */
@Component
public class CalcularDisponivel {

    /**
     * @param quantidadeEmEstoque saldo em mãos do livro, não negativo
     * @param quantidadeReservada soma das reservas ativas do livro, não negativa
     * @return o disponível, no mínimo zero
     * @throws IllegalArgumentException se alguma das quantidades for negativa
     */
    public int calcular(int quantidadeEmEstoque, int quantidadeReservada) {
        if (quantidadeEmEstoque < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }
        if (quantidadeReservada < 0) {
            throw new IllegalArgumentException("A quantidade reservada não pode ser negativa.");
        }
        return Math.max(0, quantidadeEmEstoque - quantidadeReservada);
    }
}
