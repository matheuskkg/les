package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemRecomendado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;

/**
 * Valida a saída da IA antes de gravar (RNF-REC-001, DEC-03): só valem os ids que estavam entre os candidatos, cada
 * livro uma vez, na ordem devolvida, e a justificativa é cortada em {@value #TAMANHO_DA_JUSTIFICATIVA} caracteres (code points)
 * (tamanho da coluna). Justificativa em branco vira nula.
 */
@Component
public class FiltrarItensRecomendados {

    public static final int TAMANHO_DA_JUSTIFICATIVA = 300;

    public List<ItemDaRecomendacao> filtrar(List<ItemRecomendado> devolvidos, List<LivroCandidato> candidatos) {
        Set<Long> validos = new HashSet<>(candidatos.stream().map(LivroCandidato::id).toList());
        Set<Long> usados = new HashSet<>();
        List<ItemDaRecomendacao> itens = new ArrayList<>();
        for (ItemRecomendado item : devolvidos) {
            if (item.livroId() != null && validos.contains(item.livroId()) && usados.add(item.livroId())) {
                itens.add(ItemDaRecomendacao.novo(item.livroId(), itens.size() + 1, justificativa(item)));
            }
        }
        return itens;
    }

    private static String justificativa(ItemRecomendado item) {
        String texto = item.justificativa();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpo = texto.strip();
        // Corta por code point, para não partir um emoji ao meio.
        return limpo.codePointCount(0, limpo.length()) <= TAMANHO_DA_JUSTIFICATIVA
                ? limpo
                : limpo.substring(0, limpo.offsetByCodePoints(0, TAMANHO_DA_JUSTIFICATIVA));
    }
}
