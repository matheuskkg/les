package br.fatec.les.livraria.vendas.aplicacao;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarCliente;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteDetalhado;
import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Copia o cliente para o pedido: nome, CPF, e-mail e telefone do momento da compra (DEC-12, DEC-16). */
@Component
@RequiredArgsConstructor
public class CopiarClienteDoPedido {

    private final ConsultarCliente consultarCliente;

    @Transactional(readOnly = true)
    public ClienteNoPedido executar(Long clienteId) {
        ClienteDetalhado detalhado = consultarCliente.executar(clienteId);
        Telefone telefone = detalhado.contato().telefone();
        return new ClienteNoPedido(
                clienteId,
                detalhado.cliente().getNome(),
                detalhado.cliente().getCpf().numero(),
                detalhado.contato().email(),
                telefone == null ? null : telefone.formatado());
    }
}
