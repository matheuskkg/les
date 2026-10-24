package br.fatec.les.livraria.vendas.dominio.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** Um cupom promocional por compra, de qualquer tipo, inclusive os de frete (RN-VEN-003, DEC-11). */
@Component
public class UmCupomPromocionalPorCompra implements Regra<List<String>> {

    @Override
    public void validar(List<String> codigos, Violacoes violacoes) {
        if (codigos.size() > 1) {
            violacoes.adicionar("RN-VEN-003", "Use apenas um cupom promocional por compra.");
        }
    }
}
