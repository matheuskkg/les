package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomAbatido;

/** Cupom de troca usado no pagamento e quanto dele cobriu o total (DEC-11). */
public record CupomAbatidoDto(Long id, String codigo, Dinheiro valor, Dinheiro abatido) {

    public static CupomAbatidoDto de(CupomAbatido cupom) {
        return new CupomAbatidoDto(cupom.cupom().id(), cupom.cupom().codigo(), cupom.cupom().valor(), cupom.abatido());
    }
}
