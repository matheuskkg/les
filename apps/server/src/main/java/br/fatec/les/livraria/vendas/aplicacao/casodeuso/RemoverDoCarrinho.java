package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.LiberarReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import lombok.RequiredArgsConstructor;

/**
 * Remove um livro do carrinho e libera o bloqueio correspondente (RF-VEN-001, DEC-15.3). O carrinho vazio
 * deixa de ter prazo de expiração. O item é retirado só depois da liberação, então uma falha não deixa
 * estoque bloqueado sem item.
 */
@Component
@RequiredArgsConstructor
public class RemoverDoCarrinho {

    private final IdentificarCliente identificarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final LiberarReserva liberarReserva;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @param livroId livro a remover
     * @return o carrinho atualizado
     * @throws ExcecaoDeRecursoNaoEncontrado se o carrinho ou o item não existirem
     */
    @Transactional
    public CarrinhoConsultado executar(Long livroId) {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho = carrinhos.buscarPorClienteParaAlterar(clienteId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Carrinho não encontrado."));
        carrinho.item(livroId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Item não encontrado no carrinho."));

        liberarReserva.executar(OrigemReserva.CARRINHO, carrinho.getId(), livroId);

        carrinho.remover(livroId);
        if (carrinho.vazio()) {
            carrinho.encerrarExpiracao();
        }
        Carrinho salvo = carrinhos.salvar(carrinho);
        return montarCarrinho.montar(salvo);
    }
}
