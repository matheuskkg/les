package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao {@code recomendacao} as unidades vendidas por livro nos pedidos que contam como venda, já somadas no
 * banco com desconto das trocas concluídas (RNF-REC-001, DEC-03, DEC-12, RNF-GER-001): é a base da ordenação pelos
 * mais vendidos.
 */
@Component
@RequiredArgsConstructor
public class ConsultarUnidadesVendidas {

    private final VendasConsultas vendas;

    /**
     * @return unidades líquidas por id de livro; livro sem saldo líquido não aparece
     */
    @Transactional(readOnly = true)
    public Map<Long, Integer> executar() {
        return vendas.unidadesVendidasPorLivro();
    }
}
