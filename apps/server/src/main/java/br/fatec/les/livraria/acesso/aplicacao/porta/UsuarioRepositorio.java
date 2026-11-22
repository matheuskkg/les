package br.fatec.les.livraria.acesso.aplicacao.porta;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    /**
     * Carrega vários usuários de uma vez, para que as listagens de outros módulos não façam uma consulta por item
     * (RF-CLI-004, DEC-07).
     */
    List<Usuario> buscarPorIds(Collection<Long> ids);

    /**
     * Ids dos usuários cujo e-mail contém o termo, sem diferenciar maiúsculas, e/ou cujo telefone (DDD + número) é
     * igual aos dígitos informados. E-mail e telefone se combinam com E; um valor nulo não filtra (RF-CLI-004, DEC-07).
     *
     * @param termoDoEmail trecho do e-mail já em minúsculas, ou nulo
     * @param digitosDoTelefone DDD + número já validados, ou nulo
     */
    Set<Long> buscarIdsPorContato(String termoDoEmail, String digitosDoTelefone);

    /**
     * Ids dos usuários nas situações informadas no instante {@code agora}, calculadas sobre {@code inativado_em}
     * (RF-CLI-004, DEC-10).
     */
    Set<Long> listarIdsPorSituacao(Collection<SituacaoConta> situacoes, LocalDateTime agora);

    /** {@code true} se um e-mail pertence a uma conta em uso. */
    boolean existeEmailEmUso(String email);

    /**
     * {@code true} se um e-mail pertence a outra conta em uso, ignorando o usuário informado. Usada na alteração dos
     * dados do cliente, em que o próprio e-mail não pode bloquear a operação (RF-CLI-002, DEC-10).
     */
    boolean existeEmailEmUsoExceto(String email, Long usuarioId);

    /** {@code true} se um telefone (só dígitos) pertence a uma conta em uso. */
    boolean existeTelefoneEmUso(String telefone);

    /** {@code true} se um telefone (só dígitos) pertence a outra conta em uso, ignorando o usuário informado. */
    boolean existeTelefoneEmUsoExceto(String telefone, Long usuarioId);

    /**
     * {@code true} se algum dos usuários informados pertence a uma conta em uso. Usada pela unicidade de CPF do módulo
     * {@code clientes}, que pergunta ao {@code acesso} antes de liberar um CPF de conta encerrada (DEC-10).
     */
    boolean existeAlgumaEmUso(Collection<Long> ids);

    List<Long> listarAdministradores();
}
