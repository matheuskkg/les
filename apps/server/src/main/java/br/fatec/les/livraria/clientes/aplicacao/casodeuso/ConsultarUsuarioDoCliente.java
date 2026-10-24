package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Usuário de login de um cliente (DEC-13). O {@code vendas} o usa para notificar o cliente fora de uma requisição,
 * como na tarefa de expiração do carrinho, em que não há sessão (RN-VEN-014).
 */
@Component
@RequiredArgsConstructor
public class ConsultarUsuarioDoCliente {

    private final ClienteRepositorio clientes;

    /**
     * @param clienteId id do cliente
     * @return id do usuário de login do cliente
     * @throws ExcecaoDeRecursoNaoEncontrado se o cliente não existir
     */
    @Transactional(readOnly = true)
    public Long executar(Long clienteId) {
        return clientes.buscarUsuarioIdPorClienteId(clienteId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
    }
}
