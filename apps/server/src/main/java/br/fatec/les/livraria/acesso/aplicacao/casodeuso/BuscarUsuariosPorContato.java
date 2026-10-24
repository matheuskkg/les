package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
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
     * @param telefone telefone com ou sem máscara (e com ou sem +55); nulo ou em branco não filtra
     * @return vazio quando nenhum dos dois foi informado (sem filtro); senão os ids encontrados, possivelmente
     *         nenhum
     * @throws ParametroInvalido se o telefone não tiver DDD e número (HTTP 400, DEC-07)
     */
    @Transactional(readOnly = true)
    public Optional<Set<Long>> executar(String email, String telefone) {
        String termo = email == null || email.isBlank() ? null : email.strip().toLowerCase(Locale.ROOT);
        String digitos = telefone == null || telefone.isBlank() ? null : digitosDoTelefone(telefone);
        if (termo == null && digitos == null) {
            return Optional.empty();
        }
        return Optional.of(usuarios.buscarIdsPorContato(termo, digitos));
    }

    private static String digitosDoTelefone(String telefone) {
        String digitos = Telefone.digitosNacionais(telefone);
        if (!Telefone.digitosValidos(digitos)) {
            throw new ParametroInvalido("telefone", "O telefone deve ter DDD e número, com 10 ou 11 dígitos.");
        }
        return digitos;
    }
}
