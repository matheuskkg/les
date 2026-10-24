package br.fatec.les.livraria.estoque.aplicacao.porta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * Porta de saída para os saldos e as reservas dos livros (DEC-15.3). Cada método é resolvido por uma única
 * consulta agregada, para que a disponibilidade de uma página inteira não gere uma consulta por livro.
 */
public interface SaldoDeEstoque {

    /**
     * @param livroIds livros a consultar
     * @return quantidade em mãos por id de livro; livro sem linha de saldo não aparece no mapa
     */
    Map<Long, Integer> quantidadesEmEstoque(Collection<Long> livroIds);

    /**
     * @param livroIds livros a consultar
     * @param agora    instante usado para considerar uma reserva ativa
     * @return soma das reservas ativas por id de livro; livro sem reserva ativa não aparece no mapa
     */
    Map<Long, Integer> reservasAtivas(Collection<Long> livroIds, LocalDateTime agora);
}
