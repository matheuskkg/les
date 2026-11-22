package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.CredenciaisInvalidas;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Login por e-mail ou telefone (DEC-08, DEC-10, DEC-15.4). Conta ativa com a senha correta entra; conta inativa nesse
 * mesmo caso é reativada; conta encerrada, senha errada ou login desconhecido recebem o mesmo erro.
 */
@Component
@RequiredArgsConstructor
public class Autenticar {

    /**
     * Hash BCrypt válido e fixo, conferido quando o login não existe ou a conta está encerrada, para que o tempo de
     * resposta não revele se o identificador existe (DEC-15.4).
     */
    private static final String HASH_FICTICIO = "$2a$12$tBL/lwtgoCE9aT4jnhIMt.708q6gcto91xPB4CKYHcrN5zYPrys.e";

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha senhas;
    private final Relogio relogio;
    private final SituacaoDaConta situacaoDaConta;

    /**
     * Sem {@code @Transactional} de propósito: o BCrypt não deve segurar uma transação/consulta aberta. A gravação da
     * reativação é transacional no próprio {@code save} do Spring Data.
     */
    public Usuario executar(Credenciais credenciais) {
        if (credenciais == null || credenciais.login() == null || credenciais.senha() == null) {
            throw new CredenciaisInvalidas();
        }
        Usuario usuario = usuarios.buscarPorLogin(credenciais.login()).orElse(null);
        SituacaoConta situacao = usuario == null ? null : situacaoDaConta.calcular(usuario, relogio.agora());
        if (usuario == null || situacao == SituacaoConta.ENCERRADA) {
            senhas.confere(credenciais.senha(), HASH_FICTICIO);
            throw new CredenciaisInvalidas();
        }
        if (!senhas.confere(credenciais.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidas();
        }
        if (situacao == SituacaoConta.INATIVA) {
            usuario.reativar(); // RF-CLI-003
            usuarios.salvar(usuario);
        }
        return usuario;
    }

    /** Credenciais de login (DEC-10): o identificador é o e-mail (com {@code @}) ou o telefone. */
    public record Credenciais(String login, String senha) {}
}
