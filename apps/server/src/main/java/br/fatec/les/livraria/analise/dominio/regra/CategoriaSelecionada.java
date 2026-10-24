package br.fatec.les.livraria.analise.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** A análise compara uma ou mais categorias; ao menos uma deve ser selecionada (RF-ANA-003). */
@Component
public class CategoriaSelecionada implements Regra<SolicitacaoDeAnalise> {

    @Override
    public void validar(SolicitacaoDeAnalise alvo, Violacoes violacoes) {
        if (alvo.categorias().isEmpty()) {
            violacoes.adicionar("RF-ANA-003", "Selecione ao menos uma categoria.");
        }
    }
}
