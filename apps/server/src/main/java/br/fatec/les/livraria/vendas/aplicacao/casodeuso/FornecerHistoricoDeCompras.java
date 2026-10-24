package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.porta.HistoricoDeCompras;
import br.fatec.les.livraria.clientes.dominio.ranking.CompraDoCliente;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Entrega ao módulo {@code clientes}, pela porta {@link HistoricoDeCompras}, as compras usadas no ranking: pedidos
 * {@code APROVADA}, {@code EM TRANSPORTE} e {@code ENTREGUE}, com o total menos os cupons gerados pelas trocas
 * concluídas (RN-CLI-007, DEC-04, DEC-12).
 */
@Component
@RequiredArgsConstructor
public class FornecerHistoricoDeCompras implements HistoricoDeCompras {

    private final VendasConsultas vendas;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<CompraDoCliente>> comprasDesde(Collection<Long> clienteIds, LocalDateTime desde) {
        Objects.requireNonNull(clienteIds, "clienteIds");
        Objects.requireNonNull(desde, "desde");
        return vendas.comprasDosClientes(clienteIds, desde);
    }
}
