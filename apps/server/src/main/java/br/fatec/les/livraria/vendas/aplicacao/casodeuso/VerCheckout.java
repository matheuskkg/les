package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.vendas.aplicacao.MontarCheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CheckoutConsultado;
import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import lombok.RequiredArgsConstructor;

/** Consulta o checkout aberto do cliente autenticado (RF-VEN-003, DEC-07, DEC-12). */
@Component
@RequiredArgsConstructor
public class VerCheckout {

    private final IdentificarCliente identificarCliente;
    private final CheckoutRepositorio checkouts;
    private final MontarCheckoutConsultado montarCheckout;

    /** @throws ExcecaoDeRecursoNaoEncontrado se não houver checkout aberto */
    @Transactional(readOnly = true)
    public CheckoutConsultado executar() {
        Long clienteId = identificarCliente.executar();
        return checkouts.buscarAbertoPorCliente(clienteId)
                .map(montarCheckout::montar)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Nenhuma compra em andamento."));
    }
}
