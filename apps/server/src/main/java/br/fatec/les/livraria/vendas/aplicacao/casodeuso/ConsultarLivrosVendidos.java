package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao {@code recomendacao} os livros de todos os pedidos que contam como venda, com o cliente e o pedido
 * (RNF-REC-001, DEC-03, DEC-12): o perfil, a co-compra e os mais vendidos saem daqui, sem que o outro módulo acesse
 * a persistência de {@code vendas}.
 */
@Component
@RequiredArgsConstructor
public class ConsultarLivrosVendidos {

    private final VendasConsultas vendas;

    /**
     * @return os itens dos pedidos {@code APROVADA}, {@code EM TRANSPORTE} e {@code ENTREGUE}, com a quantidade
     *         líquida das trocas concluídas; itens trocados por inteiro não aparecem
     */
    @Transactional(readOnly = true)
    public List<LivroVendido> executar() {
        return vendas.livrosVendidos();
    }
}
