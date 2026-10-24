package br.fatec.les.livraria.acesso.infraestrutura.web.dto;

import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

/** Sessão do usuário logado (DEC-08), devolvida no login e na consulta da sessão. */
public record Sessao(Long usuarioId, String email, Papel papel) {

    public static Sessao de(Usuario usuario) {
        return new Sessao(usuario.getId(), usuario.getEmail(), usuario.getPapel());
    }
}
