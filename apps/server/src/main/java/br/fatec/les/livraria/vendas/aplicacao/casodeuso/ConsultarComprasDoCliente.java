package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao {@code recomendacao} as compras de um cliente nos pedidos que contam como venda (RNF-REC-001, DEC-03,
 * DEC-12): o perfil do chat e a validade da recomendação saem daqui, filtrando pelo cliente no banco
 * (RNF-GER-001), sem que o outro módulo acesse a persistência de {@code vendas}.
 */
@Component
@RequiredArgsConstructor
public class ConsultarComprasDoCliente {

    private final VendasConsultas vendas;

    /**
     * @param clienteId cliente das compras; não pode ser nulo
     * @return os itens dos pedidos {@code APROVADA}, {@code EM TRANSPORTE} e {@code ENTREGUE} do cliente, com a
     *         quantidade líquida das trocas concluídas; itens trocados por inteiro não aparecem
     */
    @Transactional(readOnly = true)
    public List<LivroVendido> executar(Long clienteId) {
        Objects.requireNonNull(clienteId, "clienteId");
        return vendas.comprasDoCliente(clienteId);
    }
}
