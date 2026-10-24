package br.fatec.les.livraria.vendas.aplicacao;

import java.util.ArrayList;
import java.util.List;

import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomAbatido;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;
import br.fatec.les.livraria.vendas.dominio.pedido.CartaoNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.CategoriaCupom;
import br.fatec.les.livraria.vendas.dominio.pedido.CupomNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.CupomPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.EnderecoNoPedido;

/** Monta as cópias do pedido a partir do cadastro do cliente e dos cupons da compra (DEC-10, DEC-12, DEC-16). */
public final class CopiasDoPedido {

    private CopiasDoPedido() {
    }

    public static EnderecoNoPedido endereco(Endereco endereco) {
        return new EnderecoNoPedido(endereco.id(), endereco.nomeCurto(), endereco.tipoEndereco().name(),
                endereco.tipoLogradouro().name(), endereco.logradouro(), endereco.numero(), endereco.bairro(),
                endereco.cep(), endereco.cidade(), endereco.uf().name(), endereco.pais(), endereco.observacoes());
    }

    /** Só bandeira e 4 últimos dígitos (DEC-10). */
    public static CartaoNoPedido cartao(Cartao cartao) {
        String numero = cartao.numero();
        return new CartaoNoPedido(cartao.id(), cartao.bandeira().nome(), numero.substring(numero.length() - 4));
    }

    /** O promocional abate o desconto, e cada cupom de troca, o que cobriu do total (DEC-11). */
    public static List<CupomPedido> cupons(DistribuicaoDoPagamento distribuicao) {
        List<CupomPedido> cupons = new ArrayList<>();
        CupomPromocional promocional = distribuicao.promocional();
        if (promocional != null) {
            cupons.add(new CupomPedido(new CupomNoPedido(promocional.id(), promocional.codigo(),
                    CategoriaCupom.PROMOCIONAL, promocional.tipo().name()), distribuicao.descontoPromocional()));
        }
        for (CupomAbatido abatido : distribuicao.cuponsDeTroca()) {
            cupons.add(new CupomPedido(new CupomNoPedido(abatido.cupom().id(), abatido.cupom().codigo(),
                    CategoriaCupom.TROCA, null), abatido.abatido()));
        }
        return cupons;
    }
}
