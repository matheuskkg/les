package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.CompraRegistrada;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Quem comprou X também comprou Y" (RNF-REC-001, DEC-03): para cada livro, quantos outros clientes que compraram algum
 * livro do cliente também o compraram. Cada cliente conta uma vez por livro.
 */
@Component
public class ContarCoCompras {

    /**
     * @param clienteId cliente da recomendação
     * @param compras compras de todos os clientes
     * @return número de outros clientes por livro; livros que o próprio cliente comprou também aparecem
     */
    public Map<Long, Integer> contar(Long clienteId, List<CompraRegistrada> compras) {
        Map<Long, Set<Long>> livrosPorCliente =
                compras.stream()
                        .collect(
                                Collectors.groupingBy(
                                        CompraRegistrada::clienteId,
                                        Collectors.mapping(CompraRegistrada::livroId, Collectors.toSet())));
        Set<Long> doCliente = livrosPorCliente.getOrDefault(clienteId, Set.of());
        Map<Long, Integer> coCompras = new HashMap<>();
        if (doCliente.isEmpty()) {
            return coCompras;
        }
        livrosPorCliente.forEach((outro, livros) -> {
            if (outro.equals(clienteId)) {
                return;
            }
            Set<Long> emComum = new HashSet<>(livros);
            emComum.retainAll(doCliente);
            if (!emComum.isEmpty()) {
                livros.forEach(livro -> coCompras.merge(livro, 1, Integer::sum));
            }
        });
        return coCompras;
    }
}
