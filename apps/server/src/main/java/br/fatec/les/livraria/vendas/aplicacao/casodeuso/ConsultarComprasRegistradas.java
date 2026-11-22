package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraDeLivro;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Expõe ao {@code recomendacao} as compras de todos os clientes nos pedidos que contam como venda, já somadas por
 * cliente e livro no banco (RNF-REC-001, DEC-03, DEC-12, RNF-GER-001): a co-compra "quem comprou X também comprou Y"
 * sai daqui, sem carregar item por item.
 */
@Component
@RequiredArgsConstructor
public class ConsultarComprasRegistradas {

    private final VendasConsultas vendas;

    /**
     * @return um registro por par cliente–livro–cópia do livro no pedido, com a quantidade líquida das trocas
     *     concluídas; pares trocados por inteiro não aparecem. O agrupamento é pela cópia em JSON do snapshot (DEC-16),
     *     então cópias divergentes do mesmo livro — caso teórico enquanto a edição de livros está fora do MVP — sairiam
     *     em registros distintos do mesmo par cliente–livro; quem consome, a co-compra {@code ContarCoCompras}, mapeia
     *     para conjunto por cliente e é imune a essa multiplicidade.
     */
    @Transactional(readOnly = true)
    public List<CompraDeLivro> executar() {
        return vendas.comprasRegistradas();
    }
}
