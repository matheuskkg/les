package br.fatec.les.livraria.analise.dominio.modelo;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import java.util.List;
import java.util.Objects;

/**
 * Linha do gráfico de uma categoria: um valor por mês do período, na ordem dos meses (RNF-ANA-001, RN-ANA-003).
 *
 * @param categoria categoria da linha
 * @param valores total vendido em cada mês, R$ 0,00 nos meses sem venda
 */
public record SerieDeVendas(CategoriaAnalisada categoria, List<Dinheiro> valores) {

    public SerieDeVendas {
        Objects.requireNonNull(categoria, "categoria");
        valores = List.copyOf(valores);
    }
}
