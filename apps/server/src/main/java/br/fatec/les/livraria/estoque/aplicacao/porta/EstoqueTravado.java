package br.fatec.les.livraria.estoque.aplicacao.porta;

import java.util.Optional;

/**
 * Porta de saída da linha de saldo com lock pessimista (DEC-15.3). Travar a linha antes de conferir o
 * disponível é o que impede duas requisições simultâneas de venderem além do estoque (RN-VEN-001).
 */
public interface EstoqueTravado {

    /**
     * Trava a linha do saldo do livro até o fim da transação.
     *
     * @param livroId livro cuja linha será travada
     * @return quantidade em mãos, ou vazio quando o livro não tem linha de saldo
     */
    Optional<Integer> quantidadeComLock(Long livroId);

    /**
     * Grava a nova quantidade em mãos do livro, cuja linha já foi travada por {@link #quantidadeComLock(Long)} na
     * mesma transação (RF-EST-003).
     */
    void gravarQuantidade(Long livroId, int quantidade);
}
