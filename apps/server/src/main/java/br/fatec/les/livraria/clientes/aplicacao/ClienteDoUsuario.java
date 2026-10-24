package br.fatec.les.livraria.clientes.aplicacao;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Cliente do usuário autenticado (DEC-07): as rotas {@code /api/me} nunca recebem o id do cliente, só o do usuário da
 * sessão. Cliente inexistente responde 404.
 */
@Component
@RequiredArgsConstructor
public class ClienteDoUsuario {

    private final ClienteRepositorio clientes;

    public Cliente buscar(Long usuarioId) {
        return clientes.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
    }
}
