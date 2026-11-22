package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Consulta pública do {@code acesso} para as unicidades do cadastro de clientes (DEC-10): e-mail e telefone valem entre
 * as contas em uso, e o CPF só continua bloqueado enquanto o usuário do cliente com aquele CPF não estiver encerrado. A
 * definição do prazo fica no {@code acesso}; os outros módulos não a conhecem.
 */
@Component
@RequiredArgsConstructor
public class ContasEmUso {

    private final UsuarioRepositorio usuarios;

    /** {@code true} se o e-mail pertence a uma conta ativa ou inativa dentro do prazo de reativação. */
    @Transactional(readOnly = true)
    public boolean emailEmUso(String email) {
        return usuarios.existeEmailEmUso(email);
    }

    /** {@code true} se o e-mail pertence a outra conta em uso, ignorando o usuário que está sendo alterado. */
    @Transactional(readOnly = true)
    public boolean emailEmUsoExceto(String email, Long usuarioId) {
        return usuarios.existeEmailEmUsoExceto(email, usuarioId);
    }

    /** {@code true} se o telefone (DDD + número) pertence a uma conta em uso, sem considerar o tipo. */
    @Transactional(readOnly = true)
    public boolean telefoneEmUso(String ddd, String numero) {
        return usuarios.existeTelefoneEmUso(ddd + numero);
    }

    /** {@code true} se o telefone pertence a outra conta em uso, ignorando o usuário que está sendo alterado. */
    @Transactional(readOnly = true)
    public boolean telefoneEmUsoExceto(String ddd, String numero, Long usuarioId) {
        return usuarios.existeTelefoneEmUsoExceto(ddd + numero, usuarioId);
    }

    /** {@code true} se algum dos usuários informados ainda está em uso (conta encerrada libera CPF, DEC-10). */
    @Transactional(readOnly = true)
    public boolean algumaEmUso(Collection<Long> usuarioIds) {
        return usuarios.existeAlgumaEmUso(usuarioIds);
    }
}
