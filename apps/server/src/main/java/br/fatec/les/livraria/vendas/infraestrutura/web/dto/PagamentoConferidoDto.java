package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.PagamentoConferido;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;

/**
 * Distribuição do pagamento conferido (RF-VEN-007, RN-VEN-006, DEC-11): total, desconto do promocional, cupons de
 * troca, troco, restante para os cartões e os cartões, se informados.
 */
public record PagamentoConferidoDto(
        Dinheiro subtotal,
        Dinheiro frete,
        Dinheiro total,
        Dinheiro descontoPromocional,
        List<CupomAbatidoDto> cuponsDeTroca,
        Dinheiro troco,
        Dinheiro restante,
        List<CartaoConferidoDto> cartoes) {

    public static PagamentoConferidoDto de(PagamentoConferido conferido) {
        DistribuicaoDoPagamento distribuicao = conferido.distribuicao();
        return new PagamentoConferidoDto(distribuicao.subtotal(), distribuicao.frete(), distribuicao.total(),
                distribuicao.descontoPromocional(),
                distribuicao.cuponsDeTroca().stream().map(CupomAbatidoDto::de).toList(), distribuicao.troco(),
                distribuicao.restante(), conferido.cartoes().stream().map(CartaoConferidoDto::de).toList());
    }
}
