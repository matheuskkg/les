package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.CompraRegistrada;

/** Unidades vendidas por livro, para ordenar os mais vendidos quando o cliente não tem histórico (RNF-REC-001, DEC-03). */
@Component
public class ContarVendidos {

    public Map<Long, Integer> contar(List<CompraRegistrada> compras) {
        return compras.stream().collect(Collectors.groupingBy(CompraRegistrada::livroId,
                Collectors.summingInt(CompraRegistrada::quantidade)));
    }
}
