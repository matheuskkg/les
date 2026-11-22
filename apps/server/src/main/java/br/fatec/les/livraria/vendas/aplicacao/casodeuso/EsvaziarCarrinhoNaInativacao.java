package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.EfeitoDaInativacao;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarClienteDoUsuario;
import br.fatec.les.livraria.vendas.aplicacao.LiberarItensDoCarrinho;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;

import lombok.RequiredArgsConstructor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Esvazia o carrinho e libera as reservas quando o cliente inativa a conta (DEC-10). É chamado pelo {@code
 * InativarConta} do {@code acesso}, na mesma transação, pela porta {@link EfeitoDaInativacao}. Roda antes do {@link
 * EncerrarCheckoutNaInativacao}: as travas seguem a ordem carrinho, depois checkout.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class EsvaziarCarrinhoNaInativacao implements EfeitoDaInativacao {

    private final ConsultarClienteDoUsuario consultarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final LiberarItensDoCarrinho liberarItens;

    @Override
    @Transactional
    public void aplicar(Long usuarioId) {
        consultarCliente.executar(usuarioId).flatMap(carrinhos::buscarPorClienteParaAlterar).ifPresent(this::esvaziar);
    }

    private void esvaziar(Carrinho carrinho) {
        liberarItens.executar(carrinho);
        carrinho.esvaziar();
        carrinhos.salvar(carrinho);
    }
}
