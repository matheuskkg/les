package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava o e-mail e o telefone do usuário (RF-CLI-002, DEC-08, DEC-10). É a porta de entrada pública que o módulo {@code
 * clientes} usa na alteração do cadastro: a unicidade é validada antes, no módulo {@code clientes}, e o {@code acesso}
 * continua dono da persistência do {@code Usuario}.
 */
@Component
@RequiredArgsConstructor
public class AlterarContato {

    private final UsuarioRepositorio usuarios;

    /**
     * @return o contato como ficou gravado, com o e-mail normalizado
     */
    @Transactional
    public DadosDeContato executar(Long usuarioId, DadosDeContato contato) {
        Usuario usuario =
                usuarios.buscarPorId(usuarioId)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Usuário não encontrado."));
        Telefone telefone =
                new Telefone(
                        contato.tipoTelefone(),
                        Telefone.somenteDigitos(contato.ddd()),
                        Telefone.somenteDigitos(contato.numero()));
        usuario.alterarContato(contato.email(), telefone);
        return DadosDeContato.de(usuarios.salvar(usuario));
    }
}
