package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.NomeDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Informa ao {@code acesso} o nome do cliente de um usuário, exibido na sessão (DEC-08). */
@Component
@RequiredArgsConstructor
public class InformarNomeDoCliente implements NomeDoUsuario {

    private final ClienteRepositorio clientes;

    @Override
    @Transactional(readOnly = true)
    public Optional<String> buscar(Long usuarioId) {
        return clientes.buscarPorUsuarioId(usuarioId).map(Cliente::getNome);
    }
}
