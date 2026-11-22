package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.regra.RemocaoDoUltimoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exclui fisicamente um endereço do cliente autenticado (RF-CLI-006, DEC-10). O último endereço não pode ser excluído:
 * a resposta é 422 com o código RN-CLI-001 (RN-CLI-002, DEC-10). Endereço de outro cliente responde 404 (DEC-07). Os
 * pedidos antigos não mudam, porque guardam cópia do endereço (DEC-10, DEC-16).
 */
@Component
@RequiredArgsConstructor
public class RemoverEndereco {

    private final RemocaoDoUltimoEndereco remocaoDoUltimoEndereco; // RN-CLI-001, RN-CLI-002
    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;

    /**
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se for o único endereço
     * @throws ExcecaoDeRecursoNaoEncontrado se o endereço não for do cliente autenticado
     */
    @Transactional
    public void executar(Long usuarioId, Long enderecoId) {
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        if (cliente.endereco(enderecoId).isEmpty()) {
            throw new ExcecaoDeRecursoNaoEncontrado("Endereço não encontrado.");
        }
        Violacoes.verificar(cliente, remocaoDoUltimoEndereco);
        clientes.salvar(cliente.semEndereco(enderecoId));
    }
}
