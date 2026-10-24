package br.fatec.les.livraria.acesso.aplicacao.porta;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

/**
 * Repositório de usuários em memória para os testes de caso de uso que só precisam da lista de administradores
 * (DEC-13). As demais operações não são usadas e falham se chamadas.
 */
public class UsuarioRepositorioFake implements UsuarioRepositorio {

    private final List<Long> administradores;

    public UsuarioRepositorioFake(List<Long> administradores) {
        this.administradores = List.copyOf(administradores);
    }

    @Override
    public List<Long> listarAdministradores() {
        return administradores;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        throw new UnsupportedOperationException("Fora do escopo do teste.");
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        throw new UnsupportedOperationException("Fora do escopo do teste.");
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        throw new UnsupportedOperationException("Fora do escopo do teste.");
    }

    @Override
    public boolean existeEmailEmUso(String email) {
        throw new UnsupportedOperationException("Fora do escopo do teste.");
    }

    @Override
    public boolean existeTelefoneEmUso(String telefone) {
        throw new UnsupportedOperationException("Fora do escopo do teste.");
    }

    @Override
    public boolean existeAlgumaEmUso(Collection<Long> ids) {
        throw new UnsupportedOperationException("Fora do escopo do teste.");
    }
}
