package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Itens da recomendação gravada sem IA (RNF-REC-001, DEC-03): os candidatos na ordem da pontuação, sem justificativa.
 */
@Component
public class OrdenarPelaPontuacao {

    public List<ItemDaRecomendacao> ordenar(List<LivroCandidato> candidatos) {
        List<ItemDaRecomendacao> itens = new ArrayList<>();
        for (LivroCandidato candidato : candidatos) {
            itens.add(ItemDaRecomendacao.novo(candidato.id(), itens.size() + 1, null));
        }
        return itens;
    }
}
