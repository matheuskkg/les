package br.fatec.les.livraria.estoque.aplicacao.porta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

/**
 * Porta de saída das reservas de estoque por dono (DEC-15.3, DEC-16). As reservas do carrinho e do checkout
 * são identificadas pela origem e pelo id do dono, sem que o {@code estoque} conheça esses módulos.
 */
public interface ReservasDeEstoque {

    /**
     * Busca a reserva de um livro para um dono. Cada dono tem no máximo uma reserva por livro, porque o item
     * do carrinho e do checkout é único por livro (DEC-16).
     */
    Optional<Reserva> buscar(Long livroId, OrigemReserva origem, Long origemId);

    /** Reservas de um dono, usadas para renovar o prazo de todo o carrinho (DEC-05.1). */
    List<Reserva> listarPorOrigem(OrigemReserva origem, Long origemId);

    /**
     * Reservas de um dono cujo prazo vence depois do instante informado. É só um pré-filtro de leitura,
     * nunca mais forte que a definição do domínio: nenhuma reserva ativa é excluída pela consulta, e quem
     * consulta continua aplicando {@code Reserva.ativaEm} sobre o resultado — é ela que define o que é
     * uma reserva ativa (RN-VEN-002, RNF-GER-001).
     */
    List<Reserva> listarPorOrigemVencendoDepoisDe(OrigemReserva origem, Long origemId,
            LocalDateTime vencendoDepoisDe);

    Reserva salvar(Reserva reserva);

    void remover(Long reservaId);

    /**
     * Soma das reservas <strong>ativas</strong> do livro, desconsiderando a reserva informada
     * ({@code -1} quando não há nenhuma a excluir). Excluir a reserva do próprio dono permite alterar a
     * quantidade sem contar duas vezes as unidades já bloqueadas (RN-VEN-001, RF-VEN-002).
     *
     * @param livroId        livro consultado
     * @param agora          instante usado para considerar uma reserva ativa
     * @param reservaExcluida id da reserva do próprio dono, ou {@code -1}
     * @return quantidade reservada por outros donos, não negativa
     */
    int quantidadeReservadaAtivaExceto(Long livroId, LocalDateTime agora, Long reservaExcluida);
}
