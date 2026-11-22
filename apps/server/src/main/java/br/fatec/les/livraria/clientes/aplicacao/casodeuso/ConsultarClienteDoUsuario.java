package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cliente de um usuário de login, quando o usuário não é o da sessão atual ou não precisa ser (DEC-10). O {@code
 * vendas} o usa ao esvaziar o carrinho na inativação da conta.
 */
@Component
@RequiredArgsConstructor
public class ConsultarClienteDoUsuario {

    private final ClienteRepositorio clientes;

    /**
     * @param usuarioId id do usuário de login
     * @return id do cliente, vazio se o usuário não for cliente (um administrador, por exemplo)
     */
    @Transactional(readOnly = true)
    public Optional<Long> executar(Long usuarioId) {
        return clientes.buscarIdPorUsuarioId(usuarioId);
    }
}
