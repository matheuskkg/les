package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.Ids;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consulta pública do {@code acesso} que carrega e-mail, telefone e situação de vários usuários em uma única chamada
 * (RF-CLI-004, DEC-07, DEC-10). A situação é calculada no instante informado por quem consulta; a listagem de clientes
 * do admin usa este caso de uso para enriquecer a página sem uma consulta por item.
 */
@Component
@RequiredArgsConstructor
public class ConsultarUsuarios {

    private final UsuarioRepositorio usuarios;
    private final SituacaoDaConta situacaoDaConta;

    /**
     * @param usuarioIds ids dos usuários da página; não pode ser nulo nem conter nulos
     * @param agora instante em que a situação é calculada; o filtro por situação usa o mesmo
     * @return os contatos indexados pelo id do usuário; vazio quando não há ids
     */
    @Transactional(readOnly = true)
    public Map<Long, ContatoDoUsuario> executar(Collection<Long> usuarioIds, LocalDateTime agora) {
        if (Ids.validar(usuarioIds)) {
            return Map.of();
        }
        return usuarios.buscarPorIds(usuarioIds).stream()
                .collect(Collectors.toUnmodifiableMap(Usuario::getId, usuario -> paraContato(usuario, agora)));
    }

    private ContatoDoUsuario paraContato(Usuario usuario, LocalDateTime agora) {
        return new ContatoDoUsuario(
                usuario.getId(), usuario.getEmail(), usuario.getTelefone(), situacaoDaConta.calcular(usuario, agora));
    }
}
