package br.fatec.les.livraria.vendas.dominio.pagamento;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cupom de troca do cliente (RF-VEN-007, RF-VEN-015, RN-VEN-006, DEC-11): gerado na troca ou como troco, usado inteiro
 * numa compra e só pelo dono.
 *
 * @param validade nula quando o cupom não vence (padrão de {@code livraria.cupom.troca.validade})
 */
public record CupomTroca(
        Long id,
        Long clienteId,
        String codigo,
        Dinheiro valor,
        LocalDate validade,
        SituacaoCupom situacao,
        LocalDateTime geradoEm) {

    public CupomTroca {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(situacao, "situacao");
        Objects.requireNonNull(geradoEm, "geradoEm");
    }

    public boolean vencidoEm(LocalDate hoje) {
        return validade != null && hoje.isAfter(validade);
    }

    /** Disponível para uma compra hoje (RN-VEN-007): não usado e não vencido. */
    public boolean disponivelEm(LocalDate hoje) {
        return situacao == SituacaoCupom.DISPONIVEL && !vencidoEm(hoje);
    }
}
