package br.fatec.les.livraria.vendas.dominio.pagamento;

import java.util.ArrayList;
import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

/**
 * Distribuição do pagamento (RF-VEN-007, RN-VEN-006, DEC-11), nesta ordem: o promocional abate o desconto da sua base;
 * os cupons de troca cobrem o que restou, na ordem informada, e o que passar vira troco; os cartões pagam o restante.
 * Cálculo puro: as regras de validade e de cupom desnecessário ficam em arquivos próprios.
 */
public final class DistribuirPagamento {

    private DistribuirPagamento() {
    }

    public static DistribuicaoDoPagamento de(CuponsDaCompra compra) {
        Dinheiro total = compra.total();
        Dinheiro desconto = compra.descontoPromocional();
        Dinheiro aCobrir = total.subtrair(desconto);

        List<CupomAbatido> abatidos = new ArrayList<>();
        Dinheiro somaDosCupons = Dinheiro.ZERO;
        for (CupomTroca cupom : compra.cuponsDeTroca()) {
            Dinheiro abatido = Dinheiro.menor(cupom.valor(), aCobrir);
            abatidos.add(new CupomAbatido(cupom, abatido));
            aCobrir = aCobrir.subtrair(abatido);
            somaDosCupons = somaDosCupons.somar(cupom.valor());
        }

        Dinheiro troco = Dinheiro.maior(somaDosCupons.subtrair(total.subtrair(desconto)), Dinheiro.ZERO);
        return new DistribuicaoDoPagamento(compra.subtotal(), compra.frete(), total, desconto, abatidos, troco,
                aCobrir, compra.temCupom());
    }
}
