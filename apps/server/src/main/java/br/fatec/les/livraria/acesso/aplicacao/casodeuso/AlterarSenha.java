package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.dominio.regra.ConfirmacaoDeSenha;
import br.fatec.les.livraria.acesso.dominio.regra.NovaSenhaObrigatoria;
import br.fatec.les.livraria.acesso.dominio.regra.SenhaForte;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alteração de senha do usuário autenticado (RF-CLI-008, RNF-CLI-001, RNF-CLI-002, RNF-CLI-003, DEC-08). A senha atual
 * incorreta é uma violação de negócio (422), não uma falha de autenticação, porque a sessão já está aberta. A nova
 * senha passa pelas mesmas regras do cadastro e é gravada só como hash BCrypt. A sessão atual continua válida: o token
 * leva apenas o id do usuário, e não é reemitido.
 */
@Component
@RequiredArgsConstructor
public class AlterarSenha {

    private static final String CODIGO_SENHA_ATUAL = "RF-CLI-008";

    private final NovaSenhaObrigatoria novaSenhaObrigatoria; // RNF-CLI-001
    private final SenhaForte senhaForte; // RNF-CLI-001
    private final ConfirmacaoDeSenha confirmacaoDeSenha; // RNF-CLI-002
    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha senhas; // RNF-CLI-003

    @Transactional
    public void executar(Long usuarioId, TrocaDeSenha comando) {
        Usuario usuario =
                usuarios.buscarPorId(usuarioId)
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Usuário não encontrado."));

        Violacoes violacoes = new Violacoes();
        novaSenhaObrigatoria.validar(comando, violacoes);
        senhaForte.validar(comando, violacoes);
        confirmacaoDeSenha.validar(comando, violacoes);
        if (!senhaAtualConfere(comando, usuario)) {
            violacoes.adicionar(CODIGO_SENHA_ATUAL, "A senha atual está incorreta.");
        }
        violacoes.lancarSeHouver();

        usuario.alterarSenhaHash(senhas.codificar(comando.novaSenha()));
        usuarios.salvar(usuario);
    }

    private boolean senhaAtualConfere(TrocaDeSenha comando, Usuario usuario) {
        return comando.senhaAtual() != null
                && !comando.senhaAtual().isBlank()
                && senhas.confere(comando.senhaAtual(), usuario.getSenhaHash());
    }
}
