package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import lombok.RequiredArgsConstructor;

/**
 * Consulta o carrinho do cliente autenticado (RF-VEN-001, DEC-07, DEC-15.4). Cliente sem carrinho recebe um
 * carrinho vazio, sem criar linha nova. O preço de cada item é o atual do catálogo (DEC-12, DEC-16).
 */
@Component
@RequiredArgsConstructor
public class VerCarrinho {

    private final IdentificarCliente identificarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final MontarCarrinhoConsultado montarCarrinho;

    @Transactional(readOnly = true)
    public CarrinhoConsultado executar() {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho = carrinhos.buscarPorCliente(clienteId)
                .orElseGet(() -> Carrinho.vazio(clienteId));
        return montarCarrinho.montar(carrinho);
    }
}
