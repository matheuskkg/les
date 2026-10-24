package br.fatec.les.livraria.estoque.aplicacao.porta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * Porta de saída para os saldos e as reservas dos livros (DEC-15.3). Quantidade em mãos e reservas ativas de
 * uma página inteira são lidas em uma única consulta, na mesma transação, para que a disponibilidade use um
 * único retrato do estoque e não gere uma consulta por livro.
 */
public interface SaldoDeEstoque {

    /**
     * @param livroIds livros a consultar
     * @param agora    instante usado para considerar uma reserva ativa
     * @return saldo em mãos e reservas ativas por id de livro; livro sem linha de saldo não aparece no mapa
     */
    Map<Long, SaldoDoLivro> saldos(Collection<Long> livroIds, LocalDateTime agora);
}
