package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.PrazoDeTroca;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A troca só pode ser pedida até o fim do dia {@code entregueEm} + {@code livraria.troca.prazo} (RN-VEN-013, DEC-12).
 * Pedido ainda não entregue fica com a {@link PedidoEntregueParaTroca}.
 */
@Component
public class DentroDoPrazoDeTroca implements Regra<SolicitacaoDeTroca> {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        LocalDateTime entregueEm = alvo.pedido().getEntregueEm();
        if (entregueEm != null && !PrazoDeTroca.dentro(entregueEm, alvo.prazo(), alvo.agora())) {
            violacoes.adicionar(
                    "RN-VEN-013",
                    "O prazo de troca deste pedido terminou em %s."
                            .formatted(PrazoDeTroca.ultimoDia(entregueEm, alvo.prazo()).format(DATA)));
        }
    }
}
