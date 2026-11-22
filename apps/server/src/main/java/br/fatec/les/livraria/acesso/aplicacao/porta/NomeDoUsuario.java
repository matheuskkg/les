package br.fatec.les.livraria.acesso.aplicacao.porta;

import java.util.Optional;

/**
 * Nome de exibição de um usuário, mantido em outro módulo (DEC-08). O {@code acesso} só guarda credenciais; o {@code
 * clientes} implementa esta porta com o nome do cliente.
 */
public interface NomeDoUsuario {

    /**
     * @param usuarioId usuário de login
     * @return o nome, vazio se este módulo não conhece o usuário
     */
    Optional<String> buscar(Long usuarioId);
}
