package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;

/**
 * Consulta pública do {@code acesso} que resolve os ids dos usuários por e-mail e/ou telefone (RF-CLI-004, DEC-07).
 * A consulta de clientes do admin recebe os ids e filtra {@code cliente.usuario_id IN (...)}; e-mail e telefone se
 * combinam com E, e o telefone é comparado só pelos dígitos.
 */
@Component
@RequiredArgsConstructor
public class BuscarUsuariosPorContato {

    private final UsuarioRepositorio usuarios;

    /**
     * @param email trecho do e-mail, sem diferenciar maiúsculas; nulo ou em branco não filtra
     * @param telefone telefone com ou sem máscara; nulo ou em branco não filtra
     * @return os ids encontrados; vazio quando nenhum usuário atende aos dois campos
     */
    @Transactional(readOnly = true)
    public Set<Long> executar(String email, String telefone) {
        if ((email == null || email.isBlank()) && (telefone == null || telefone.isBlank())) {
            return Set.of();
        }
        return usuarios.buscarIdsPorContato(email, telefone);
    }

    /** {@code true} quando ao menos um dos dois contatos foi informado. */
    public static boolean informado(String email, String telefone) {
        return Optional.ofNullable(email).filter(valor -> !valor.isBlank()).isPresent()
                || Optional.ofNullable(telefone).filter(valor -> !valor.isBlank()).isPresent();
    }
}
