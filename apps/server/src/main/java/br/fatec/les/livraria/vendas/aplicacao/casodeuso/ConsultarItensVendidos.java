package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemVendido;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Itens vendidos num período, por item e sem agregação, para o módulo de análise somar por mês e categoria (RF-ANA-001,
 * RN-ANA-001, RN-ANA-004, DEC-12).
 */
@Component
@RequiredArgsConstructor
public class ConsultarItensVendidos {

    private final VendasConsultas vendas;

    /**
     * @param inicio início do período, inclusive
     * @param fimExclusivo fim do período, exclusive
     * @return os itens dos pedidos {@code APROVADA}, {@code EM TRANSPORTE} e {@code ENTREGUE} finalizados no período
     */
    @Transactional(readOnly = true)
    public List<ItemVendido> executar(LocalDateTime inicio, LocalDateTime fimExclusivo) {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fimExclusivo, "fimExclusivo");
        return vendas.itensVendidos(inicio, fimExclusivo);
    }
}
