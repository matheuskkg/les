package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Consulta pública do contato do usuário (RF-CLI-002, DEC-08, DEC-10). O módulo {@code clientes} usa esta porta de
 * entrada para montar os dados do próprio cliente sem acessar a persistência do {@code acesso}.
 */
@Component
@RequiredArgsConstructor
public class ConsultarContato {

    private final UsuarioRepositorio usuarios;

    @Transactional(readOnly = true)
    public ContatoDoUsuario executar(Long usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Usuário não encontrado."));
        return ContatoDoUsuario.de(usuario);
    }
}
