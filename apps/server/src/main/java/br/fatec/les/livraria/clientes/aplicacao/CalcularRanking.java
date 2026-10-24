package br.fatec.les.livraria.clientes.aplicacao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.aplicacao.porta.HistoricoDeCompras;
import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;
import br.fatec.les.livraria.clientes.dominio.ranking.JanelaDoRanking;
import br.fatec.les.livraria.clientes.dominio.ranking.Ranking;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import lombok.RequiredArgsConstructor;

/**
 * Ranking dos clientes informados, numa só consulta ao histórico (RN-CLI-007, DEC-04, DEC-15.4). A consulta de
 * clientes do admin calcula só para os clientes da página.
 */
@Component
@RequiredArgsConstructor
public class CalcularRanking {

    private final HistoricoDeCompras historico;
    private final Relogio relogio;

    /** @return o ranking de cada cliente informado, 0 para quem não tem compras na janela */
    public Map<Long, Integer> executar(Collection<Long> clienteIds) {
        if (clienteIds.isEmpty()) {
            return Map.of();
        }
        LocalDate hoje = relogio.agora().toLocalDate();
        Map<Long, List<CompraDoCliente>> compras = historico.comprasDesde(clienteIds,
                JanelaDoRanking.inicio(hoje).atStartOfDay());
        return clienteIds.stream().distinct().collect(Collectors.toMap(Function.identity(),
                id -> Ranking.calcular(compras.getOrDefault(id, List.of()), hoje)));
    }
}
