package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

/** Cada cupom de troca entra uma vez na compra: o cupom é usado inteiro (RN-VEN-007, DEC-11). */
@Component
public class CuponsDeTrocaSemRepeticao implements Regra<List<Long>> {

    @Override
    public void validar(List<Long> cupomIds, Violacoes violacoes) {
        if (new HashSet<>(cupomIds).size() != cupomIds.size()) {
            violacoes.adicionar("RN-VEN-007", "Um cupom de troca foi informado mais de uma vez.");
        }
    }
}
