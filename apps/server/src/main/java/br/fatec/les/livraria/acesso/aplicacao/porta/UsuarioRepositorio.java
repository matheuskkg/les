package br.fatec.les.livraria.acesso.aplicacao.porta;

import java.util.List;
import java.util.Optional;

import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

/**
 * Acesso aos usuários. As consultas de login e de unicidade só encontram contas em uso, ou seja, fora da situação
 * {@code ENCERRADA} (DEC-10): o prazo de 7 dias fica escrito uma única vez, no adapter.
 */
public interface UsuarioRepositorio {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    /**
     * Busca pelo login (DEC-10): com {@code @}, é o e-mail; sem, é o telefone, comparado só pelos dígitos. O telefone
     * pode vir com o código do país ({@code +55}); ele é descartado antes da validação de 10 ou 11 dígitos.
     */
    Optional<Usuario> buscarPorLogin(String login);

    /** {@code true} se um e-mail pertence a uma conta em uso. */
    boolean existeEmailEmUso(String email);

    /** {@code true} se um telefone (só dígitos) pertence a uma conta em uso. */
    boolean existeTelefoneEmUso(String telefone);

    List<Long> listarAdministradores();
}
