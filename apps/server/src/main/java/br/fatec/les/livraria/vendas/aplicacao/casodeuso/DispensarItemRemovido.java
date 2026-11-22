package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.MontarCarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CarrinhoConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dispensa o aviso de um livro retirado do carrinho por expiração (RNF-VEN-001, DEC-16). Readicionar o livro também
 * apaga o aviso, pelo {@link AdicionarAoCarrinho}.
 */
@Component
@RequiredArgsConstructor
public class DispensarItemRemovido {

    private final IdentificarCliente identificarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final MontarCarrinhoConsultado montarCarrinho;

    /**
     * @param livroId livro retirado
     * @return o carrinho atualizado
     * @throws ExcecaoDeRecursoNaoEncontrado se o carrinho não existir ou o livro não estiver nos removidos
     */
    @Transactional
    public CarrinhoConsultado executar(Long livroId) {
        Long clienteId = identificarCliente.executar();
        Carrinho carrinho =
                carrinhos
                        .buscarPorClienteParaAlterar(clienteId)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Carrinho não encontrado."));
        carrinho.dispensarRemovido(livroId);
        return montarCarrinho.montar(carrinhos.salvar(carrinho));
    }
}
