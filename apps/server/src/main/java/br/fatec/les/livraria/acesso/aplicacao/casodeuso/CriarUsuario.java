package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import lombok.RequiredArgsConstructor;

/**
 * Cria o usuário de um cliente (DEC-08, DEC-10, RNF-CLI-003). É a porta de entrada pública que o módulo
 * {@code clientes} usa no autocadastro: o {@code Cliente} guarda só o id do usuário (DEC-16), e e-mail e telefone
 * continuam pertencendo ao {@code acesso}.
 */
@Component
@RequiredArgsConstructor
public class CriarUsuario {

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha senhas;

    /**
     * @return o id do usuário criado, com papel {@code CLIENTE} e conta ativa
     */
    @Transactional
    public Long executar(NovoUsuario comando) {
        Telefone telefone = new Telefone(comando.tipoTelefone(), comando.ddd(), comando.numero());
        Usuario usuario = new Usuario(null, comando.email(), telefone, senhas.codificar(comando.senha()),
                Papel.CLIENTE, null);
        return usuarios.salvar(usuario).getId();
    }
}
