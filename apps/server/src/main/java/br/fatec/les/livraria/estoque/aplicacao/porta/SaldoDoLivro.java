package br.fatec.les.livraria.estoque.aplicacao.porta;

/**
 * Quantidade em mãos e reservas ativas de um livro, lidas na mesma consulta para formarem um único retrato (DEC-15.3,
 * RN-EST-006).
 *
 * @param quantidadeEmEstoque saldo em mãos do livro, não negativo
 * @param quantidadeReservada soma das reservas ativas do livro, não negativa
 */
public record SaldoDoLivro(int quantidadeEmEstoque, int quantidadeReservada) {

    public static final SaldoDoLivro ZERO = new SaldoDoLivro(0, 0);
}
