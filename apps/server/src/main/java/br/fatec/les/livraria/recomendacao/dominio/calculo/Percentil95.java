package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Percentil 95 da latência pelo método do posto mais próximo (RNF-REC-001, DEC-03): o menor valor que deixa ao
 * menos 95% das amostras abaixo ou iguais a ele, sem interpolar.
 */
@Component
public class Percentil95 {

    /** @return o percentil, ou {@code null} sem amostras */
    public Integer calcular(List<Integer> amostras) {
        if (amostras.isEmpty()) {
            return null;
        }
        List<Integer> ordenadas = amostras.stream().sorted().toList();
        int posto = (int) Math.ceil(0.95 * ordenadas.size());
        return ordenadas.get(posto - 1);
    }
}
