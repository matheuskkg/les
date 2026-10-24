package br.fatec.les.livraria.acesso.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.clientes.ClientesDeTeste;

/**
 * Repositório de usuários em memória para os testes de caso de uso que só precisam da lista de administradores
 * (DEC-13). As demais operações vêm do fake completo de {@link ClientesDeTeste}.
 */
public class UsuarioRepositorioFake extends ClientesDeTeste.FakeUsuarioRepositorio {

    private final List<Long> administradores;

    public UsuarioRepositorioFake(List<Long> administradores) {
        this.administradores = List.copyOf(administradores);
    }

    @Override
    public List<Long> listarAdministradores() {
        return administradores;
    }
}
