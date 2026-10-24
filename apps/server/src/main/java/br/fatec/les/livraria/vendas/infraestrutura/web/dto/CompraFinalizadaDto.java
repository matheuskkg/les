package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.Instant;
import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioDoSistema;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraFinalizada;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/**
 * Resultado de "Finalizar compra" (RF-VEN-008, RN-VEN-008, DEC-13): o pedido no status final, {@code APROVADA} ou
 * {@code REPROVADA}, e os cartões recusados, vazios na aprovação. {@code realizadoEm} em UTC.
 */
public record CompraFinalizadaDto(Long pedidoId, StatusPedido status, Instant realizadoEm, Dinheiro total,
        List<CartaoRecusadoDto> recusas) {

    public static CompraFinalizadaDto de(CompraFinalizada compra) {
        return new CompraFinalizadaDto(compra.pedido().getId(), compra.pedido().getStatus(),
                compra.pedido().getRealizadoEm().atZone(RelogioDoSistema.FUSO).toInstant(), compra.pedido().getTotal(),
                compra.recusas().stream().map(CartaoRecusadoDto::de).toList());
    }
}
