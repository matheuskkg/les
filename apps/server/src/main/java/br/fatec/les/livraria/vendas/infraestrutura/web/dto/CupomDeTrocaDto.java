package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;

/** Cupom de troca disponível do cliente (RF-VEN-007, DEC-11); sem validade, não vence. */
public record CupomDeTrocaDto(Long id, String codigo, Dinheiro valor, @Nullable LocalDate validade) {

    public static CupomDeTrocaDto de(CupomTroca cupom) {
        return new CupomDeTrocaDto(cupom.id(), cupom.codigo(), cupom.valor(), cupom.validade());
    }
}
