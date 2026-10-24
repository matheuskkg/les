package br.fatec.les.livraria.vendas.aplicacao;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.consulta.CheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemComPreco;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemProposto;
import lombok.RequiredArgsConstructor;

/** Monta o retrato do checkout com o preço atual de cada item (RF-VEN-003, DEC-07, DEC-12). */
@Component
@RequiredArgsConstructor
public class MontarCheckoutConsultado {

    private final PrecificarItens precificarItens;

    @Transactional(readOnly = true)
    public CheckoutConsultado montar(Checkout checkout) {
        List<ItemComPreco> itens = precificarItens.executar(checkout.getItens().stream()
                .map(item -> new ItemProposto(item.livroId(), item.quantidade()))
                .toList());
        return new CheckoutConsultado(checkout.getId(), itens, PrecificarItens.subtotal(itens),
                PrecificarItens.quantidadeDeItens(itens), checkout.getIniciadoEm(), checkout.getExpiraEm());
    }
}
