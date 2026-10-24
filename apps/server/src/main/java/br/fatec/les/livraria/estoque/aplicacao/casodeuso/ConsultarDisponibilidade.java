package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.Ids;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDoLivro;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao restante do sistema o disponível de vários livros de uma vez (DEC-15.3). É a porta de entrada
 * pública do módulo {@code estoque} para a vitrine (RF-LIV-005); os outros módulos não acessam a
 * persistência de estoque.
 *
 * <p>Disponível = quantidade em estoque − reservas ativas, nunca negativo. Uma reserva é ativa quando
 * {@code expiraEm} está no futuro em relação ao {@link Relogio}. Quantidade e reservas vêm de uma única
 * consulta, na mesma transação, portanto do mesmo retrato do estoque. Livro sem linha de saldo tem
 * disponível zero.
 */
@Component
@RequiredArgsConstructor
public class ConsultarDisponibilidade {

    private final SaldoDeEstoque saldoDeEstoque;
    private final CalcularDisponivel calcularDisponivel;
    private final Relogio relogio;

    /**
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return disponível por id de livro, com zero para os livros sem saldo ou sem reserva
     */
    @Transactional(readOnly = true)
    public Map<Long, Integer> executar(Collection<Long> livroIds) {
        if (Ids.validar(livroIds)) {
            return Map.of();
        }

        LocalDateTime agora = relogio.agora();
        Map<Long, SaldoDoLivro> saldos = saldoDeEstoque.saldos(livroIds, agora);

        Map<Long, Integer> disponiveis = new LinkedHashMap<>();
        for (Long livroId : livroIds) {
            SaldoDoLivro saldo = saldos.getOrDefault(livroId, SaldoDoLivro.ZERO);
            disponiveis.put(livroId, calcularDisponivel.calcular(
                    saldo.quantidadeEmEstoque(), saldo.quantidadeReservada()));
        }
        return Map.copyOf(disponiveis);
    }
}
