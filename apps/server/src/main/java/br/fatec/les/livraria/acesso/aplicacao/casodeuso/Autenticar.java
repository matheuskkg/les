package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.CredenciaisInvalidas;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import lombok.RequiredArgsConstructor;

/**
 * Login por e-mail ou telefone (DEC-08, DEC-10, DEC-15.4). Conta ativa com a senha correta entra; conta inativa nesse
 * mesmo caso é reativada; conta encerrada, senha errada ou login desconhecido recebem o mesmo erro.
 */
@Component
@RequiredArgsConstructor
public class Autenticar {

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha senhas;
    private final Relogio relogio;
    private final SituacaoDaConta situacaoDaConta;

    @Transactional
    public Usuario executar(Credenciais credenciais) {
        if (credenciais == null || credenciais.login() == null || credenciais.senha() == null) {
            throw new CredenciaisInvalidas();
        }
        Usuario usuario = usuarios.buscarPorLogin(credenciais.login())
                .orElseThrow(CredenciaisInvalidas::new);
        if (!senhas.confere(credenciais.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidas();
        }
        SituacaoConta situacao = situacaoDaConta.calcular(usuario, relogio.agora());
        if (situacao == SituacaoConta.ENCERRADA) {
            throw new CredenciaisInvalidas();
        }
        if (situacao == SituacaoConta.INATIVA) {
            usuario.reativar(); // RF-CLI-003
            usuarios.salvar(usuario);
        }
        return usuario;
    }

    /** Credenciais de login (DEC-10): o identificador é o e-mail (com {@code @}) ou o telefone. */
    public record Credenciais(String login, String senha) {
    }
}
