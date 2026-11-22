package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marca um cartão do cliente autenticado como o único preferencial (RF-CLI-007, DEC-10). O cartão anterior deixa de ser
 * preferencial; a garantia de exatamente um preferencial fica no agregado {@link Cliente}. Cartão de outro cliente
 * responde 404 (DEC-07).
 */
@Component
@RequiredArgsConstructor
public class MarcarCartaoPreferencial {

    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;

    /**
     * @throws ExcecaoDeRecursoNaoEncontrado se o cartão não for do cliente autenticado
     */
    @Transactional
    public void executar(Long usuarioId, Long cartaoId) {
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        if (cliente.cartao(cartaoId).isEmpty()) {
            throw new ExcecaoDeRecursoNaoEncontrado("Cartão não encontrado.");
        }
        clientes.salvar(cliente.comCartaoPreferencial(cartaoId));
    }
}
