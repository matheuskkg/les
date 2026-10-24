package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.EfeitoDaInativacao;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarClienteDoUsuario;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.LiberarReserva;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Esvazia o carrinho e libera as reservas quando o cliente inativa a conta (DEC-10). É chamado pelo
 * {@code InativarConta} do {@code acesso}, na mesma transação, pela porta {@link EfeitoDaInativacao}.
 */
@Component
@RequiredArgsConstructor
public class EsvaziarCarrinhoNaInativacao implements EfeitoDaInativacao {

    private final ConsultarClienteDoUsuario consultarCliente;
    private final CarrinhoRepositorio carrinhos;
    private final LiberarReserva liberarReserva;

    @Override
    @Transactional
    public void aplicar(Long usuarioId) {
        consultarCliente.executar(usuarioId)
                .flatMap(carrinhos::buscarPorClienteParaAlterar)
                .ifPresent(this::esvaziar);
    }

    private void esvaziar(Carrinho carrinho) {
        for (ItemCarrinho item : carrinho.getItens()) {
            liberarReserva.executar(OrigemReserva.CARRINHO, carrinho.getId(), item.getLivroId());
        }
        carrinho.esvaziar();
        carrinhos.salvar(carrinho);
    }
}
