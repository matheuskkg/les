package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.SolicitacaoDeTroca;

import org.springframework.stereotype.Component;

/** A troca precisa de ao menos um item (RF-VEN-011, DEC-12). */
@Component
public class TrocaComItens implements Regra<SolicitacaoDeTroca> {

    @Override
    public void validar(SolicitacaoDeTroca alvo, Violacoes violacoes) {
        if (alvo.itens().isEmpty()) {
            violacoes.adicionar("RF-VEN-011", "Escolha ao menos um item para trocar.");
        }
    }
}
