package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

/** Lista os ids dos usuários {@code ADMIN}, usados pelo {@code NotificarAdministradores} (DEC-13). */
@Component
@RequiredArgsConstructor
public class ListarAdministradores {

    private final UsuarioRepositorio usuarios;

    public List<Long> executar() {
        return usuarios.listarAdministradores();
    }
}
