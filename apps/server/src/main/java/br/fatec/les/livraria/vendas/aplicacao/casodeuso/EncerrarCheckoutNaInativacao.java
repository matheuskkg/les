package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.EfeitoDaInativacao;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarClienteDoUsuario;
import br.fatec.les.livraria.vendas.aplicacao.LiberarItensDoCheckout;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import lombok.RequiredArgsConstructor;

/**
 * Encerra o checkout aberto e libera as reservas dele quando o cliente inativa a conta (DEC-10, DEC-12). É chamado
 * pelo {@code InativarConta} do {@code acesso}, na mesma transação, pela porta {@link EfeitoDaInativacao}.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class EncerrarCheckoutNaInativacao implements EfeitoDaInativacao {

    private final ConsultarClienteDoUsuario consultarCliente;
    private final CheckoutRepositorio checkouts;
    private final LiberarItensDoCheckout liberarItens;

    @Override
    @Transactional
    public void aplicar(Long usuarioId) {
        consultarCliente.executar(usuarioId)
                .flatMap(checkouts::buscarAbertoPorClienteParaAlterar)
                .ifPresent(this::encerrar);
    }

    private void encerrar(Checkout checkout) {
        liberarItens.executar(checkout);
        checkout.encerrar();
        checkouts.salvar(checkout);
    }
}
