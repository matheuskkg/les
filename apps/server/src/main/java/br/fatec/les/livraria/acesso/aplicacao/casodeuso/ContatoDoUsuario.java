package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

/**
 * E-mail e telefone do usuário (RF-CLI-002, DEC-08, DEC-10): os dois dados de login que ficam no {@code Usuario}. É a
 * forma pública com que o módulo {@code clientes} lê e grava o contato sem acessar a persistência do {@code acesso}.
 */
public record ContatoDoUsuario(String email, TipoTelefone tipoTelefone, String ddd, String numero) {

    public static ContatoDoUsuario de(Usuario usuario) {
        Telefone telefone = usuario.getTelefone();
        return new ContatoDoUsuario(
                usuario.getEmail(),
                telefone == null ? null : telefone.tipo(),
                telefone == null ? null : telefone.ddd(),
                telefone == null ? null : telefone.numero());
    }
}
